package io.branch.search;

import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

// TODO make package-private and access through BranchSearch
// TODO make impressions unique wrt entityID+requestID, otherwise we get duplicates
//  while scrolling or coming back from other apps. Anytime there's a new binding
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BranchImpressionTracker {

    // Do not check if we have already checked < CHECK_TIME_MILLIS ago.
    private static final long CHECK_TIME_MILLIS = 80;

    private static Map<View, BranchImpressionTracker> sTrackers = new WeakHashMap<>();

    public static void trackImpressions(@NonNull View view, @Nullable BranchLinkResult result) {
        if (Build.VERSION.SDK_INT < 18) {
            throw new IllegalStateException("Impression tracking will only work on API 18+.");
        }
        if (sTrackers.containsKey(view)) {
            BranchImpressionTracker tracker = sTrackers.get(view);
            //noinspection ConstantConditions
            tracker.bindTo(result);
        } else {
            BranchImpressionTracker tracker = new BranchImpressionTracker(view);
            tracker.bindTo(result);
            sTrackers.put(view, tracker);
        }
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
        mHasImpression = false;
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
                && now > mLastCheckMillis + CHECK_TIME_MILLIS) {
            mLastCheckMillis = now;

            // TODO: review this logic?
            //  I don't think it's correct because the first rect is in window coordinates
            //  while the other is in display coordinates.
            // TODO: add a buffer because if only 1% of the entity is visible then it's not an impression

            mView.getGlobalVisibleRect(mRect1);
            mView.getWindowVisibleDisplayFrame(mRect2);
            boolean impression = mRect1.intersect(mRect2);
            if (impression) {
                mHasImpression = true;
                Log.w("Tracker", "Got impression for [" + mResult.getName() + "]");
            } else {
                Log.i("Tracker", "No impression for [" + mResult.getName() + "]");
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
