package io.branch.search;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

// TODO make package-private and access through BranchSearch
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BranchImpressionTracker {

    // Do not check if we have already checked < CHECK_TIME_MILLIS ago.
    private static final long CHECK_TIME_MILLIS = 80;

    // Do not count as impression if the visible area is less than 50% of the full view area.
    private static final float AREA_MIN_FRACTION = 0.5F;

    private static Map<View, BranchImpressionTracker> sTrackers = new WeakHashMap<>();
    private static Set<String> sImpressionIds = new HashSet<>();

    public static void trackImpressions(@NonNull View view, @Nullable BranchLinkResult result) {
        if (Build.VERSION.SDK_INT < 18) {
            throw new IllegalStateException("Impression tracking will only work on API 18+.");
        }
        BranchImpressionTracker tracker;
        if (sTrackers.containsKey(view)) {
            tracker = sTrackers.get(view);
        } else {
            tracker = new BranchImpressionTracker(view);
            sTrackers.put(view, tracker);
        }
        //noinspection ConstantConditions
        tracker.bindTo(result);
    }

    @NonNull
    private static String getImpressionId(@NonNull BranchLinkResult result) {
        return result.getEntityID(); // TODO add request ID to avoid duplicates
    }

    private BranchLinkResult mResult = null;
    private final View mView;
    private boolean mAttached = false;
    private boolean mHasImpression = false;
    private long mLastCheckMillis = 0L;
    private final ViewTreeListener mListener = new ViewTreeListener(this);

    private BranchImpressionTracker(@NonNull View view) {
        mView = view;
        // No one will remove this attach state listener, but we don't need to.
        // This tracker is bound to the view and dies with it. No other tracker
        // will be bound to this view instance.
        mView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                onViewAttached();
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                onViewDetached();
            }
        });
        if (ViewCompat.isAttachedToWindow(mView)) {
            onViewAttached();
        }
    }

    private void bindTo(@Nullable BranchLinkResult result) {
        mResult = result;
        mHasImpression = result == null
                || sImpressionIds.contains(getImpressionId(result));
    }

    private void onViewAttached() {
        if (!mAttached) {
            // Now the ViewTreeObserver is the real one.
            mView.getViewTreeObserver().addOnScrollChangedListener(mListener);
            mView.getViewTreeObserver().addOnGlobalLayoutListener(mListener);
            mAttached = true;
        }
    }

    private void onViewDetached() {
        if (mAttached) {
            // Tear down.
            mView.getViewTreeObserver().removeOnScrollChangedListener(mListener);
            mView.getViewTreeObserver().removeOnGlobalLayoutListener(mListener);
            mAttached = false;
        }
    }

    private final Rect mRect1 = new Rect();
    private final Rect mRect2 = new Rect();

    private void checkImpression() {
        long now = System.currentTimeMillis();
        if (!mHasImpression
                && mResult != null
                && mView.isShown()
                && ViewCompat.isLaidOut(mView)
                && now > mLastCheckMillis + CHECK_TIME_MILLIS) {
            mLastCheckMillis = now;

            // mRect1: Coordinates of the visible part of the view, in WINDOW coordinates.
            // mRect2: Coordinates of the visible part of the window, in DISPLAY coordinates.
            if (!mView.getGlobalVisibleRect(mRect1)) return;
            mView.getWindowVisibleDisplayFrame(mRect2);

            // There can be a big difference between the two coordinate systems, especially
            // when in multi-window mode. We must offset one of the rects.
            int windowLeft = mRect2.left;
            int windowTop = mRect2.top;
            mRect1.offset(windowLeft, windowTop);
            Log.i("Tracker", "Checking impression for [" + mResult.getName() + "]"
                    + " viewHeight:" + mView.getHeight()
                    + ". windowHeight:" + mRect2.height() + " windowTop:" + windowTop
                    + ". visibleViewHeight:" + mRect1.height() + " visibleViewTop:" + mRect1.top);

            // Intersect the two. This is needed to take the keyboard into account.
            boolean maybe = mRect1.intersect(mRect2);
            if (maybe) {
                float fullArea = (float) mView.getWidth() * mView.getHeight();
                float visibleArea = (float) mRect1.width() * mRect1.height();
                float percentage = visibleArea / fullArea;
                if (percentage > AREA_MIN_FRACTION) {
                    mHasImpression = true;
                    sImpressionIds.add(getImpressionId(mResult));
                    Log.e("Tracker", "Got impression for [" + mResult.getName() + "]");
                } else {
                    Log.w("Tracker", "Missed impression for [" + mResult.getName() + "]. Percentage: " + percentage
                            + ". viewWidth:" + mView.getWidth() + " viewHeight:" + mView.getHeight()
                            + ". visibleWidth:" + mRect1.width() + " visibleHeight:" + mRect1.height());
                }
            }
        }
    }

    /**
     * The purpose of this static class is to be extra safe against leaks.
     * This listener will be added to the global ViewTreeObserver which lives within the root view
     * of the whole app (ViewRootImpl) that has a longer lifecycle than the View we're tracking.
     *
     * We address this by unregistering this listener when the child View is detached, which should
     * cover ALL cases. However, to be extra safe, we also use this static class combined with
     * a WeakReference to the main tracker.
     */
    private static class ViewTreeListener implements
            ViewTreeObserver.OnScrollChangedListener,
            ViewTreeObserver.OnGlobalLayoutListener {
        private final WeakReference<BranchImpressionTracker> mTracker;

        private ViewTreeListener(@NonNull BranchImpressionTracker tracker) {
            mTracker = new WeakReference<>(tracker);
        }

        @Override
        public void onScrollChanged() {
            BranchImpressionTracker tracker = mTracker.get();
            if (tracker != null) tracker.checkImpression();
        }

        @Override
        public void onGlobalLayout() {
            BranchImpressionTracker tracker = mTracker.get();
            if (tracker != null) tracker.checkImpression();
        }
    }
}
