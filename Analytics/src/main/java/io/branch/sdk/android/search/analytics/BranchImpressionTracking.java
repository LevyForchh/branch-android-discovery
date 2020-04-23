package io.branch.sdk.android.search.analytics;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.View;

import io.branch.sdk.android.search.analytics.BranchAnalytics.TrackedEntity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Coordinates impression tracking, including managing {@link BranchImpressionTracker}s,
 * batching results and uploading them to server.
 */
class BranchImpressionTracking {

    // TODO the report URL
    private static final String REPORT_URL = "https://fakeUrl.fakeUrl";

    private static Map<View, BranchImpressionTracker> sTrackers = new WeakHashMap<>();
    private static Set<JSONObject> sImpressionIds = new HashSet<>();

    private static final Object sSendLock = new Object();

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    static void trackImpressions(@NonNull View view, @Nullable TrackedEntity result) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
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

    static boolean hasTrackedImpression(@NonNull TrackedEntity result) {
        return sImpressionIds.contains(result.getTrackedEntityJson());// todo turn to hashcode for smaller memory use?
    }

    static void recordImpression(@NonNull Context context,
                                 @NonNull TrackedEntity result,
                                 float area) {
        // Record the ID so it's not saved twice.
        boolean isNew = sImpressionIds.add(result.getTrackedEntityJson());
        if (!isNew) return;

        synchronized (sSendLock) {
            // Record into shared preferences as a simple JSON string.
            JSONObject object = new JSONObject();
            try {
                object.put("entity", result.getTrackedEntityJson());
                object.put("area", area);
                object.put("timestamp", System.currentTimeMillis());
            } catch (JSONException e) {
                return;
            }
            BranchAnalytics.trackImpression(object, "search");
        }
    }
}