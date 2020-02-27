package io.branch.search;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.View;

import org.json.JSONArray;
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

    // Send impressions to server every day.
    private static final long REPORT_TIME_MILLIS = 1000L * 60 * 60 * 24;

    // But also send whenever we have more than 100 pending impressions.
    private static final int REPORT_MAX_SIZE = 100;

    // TODO the report URL
    private static final String REPORT_URL = "https://fakeUrl.fakeUrl";

    // The SharedPreferences slot where we write impressions to be uploaded.
    private static final String RECORD_KEY = "branchImpressions";

    // The SharedPreferences slot where we write the last successful upload time.
    private static final String RECORD_KEY_TIME = "branchImpressionsTime";

    private static Map<View, BranchImpressionTracker> sTrackers = new WeakHashMap<>();
    private static Set<String> sImpressionIds = new HashSet<>();

    private static URLConnectionNetworkHandler sSender = URLConnectionNetworkHandler.initialize();
    private static boolean sIsSending = false;
    private static final Object sSendLock = new Object();

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    static void trackImpressions(@NonNull View view, @Nullable BranchLinkResult result) {
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

    static boolean hasTrackedImpression(@NonNull BranchLinkResult result) {
        return sImpressionIds.contains(getImpressionId(result));
    }

    @NonNull
    private static String getImpressionId(@NonNull BranchLinkResult result) {
        return result.getRequestId() + "+" + result.getEntityID();
    }

    static void recordImpression(@NonNull Context context,
                                 @NonNull BranchLinkResult result,
                                 float area) {
        // Record the ID so it's not saved twice.
        boolean isNew = sImpressionIds.add(getImpressionId(result));
        if (!isNew) return;

        synchronized (sSendLock) {
            // Record into shared preferences as a simple JSON string.
            String impression;
            try {
                JSONObject object = new JSONObject();
                object.put("entity", result.getEntityID());
                object.put("area", area);
                object.put("timestamp", System.currentTimeMillis());
                impression = object.toString();
            } catch (JSONException e) {
                return;
            }
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            Set<String> set = preferences.getStringSet(RECORD_KEY, null);
            if (set == null) set = new HashSet<>();
            set.add(impression);
            preferences.edit().putStringSet(RECORD_KEY, set).apply();

            // Maybe send to server.
            maybeSendImpressions(preferences, set);
        }
    }

    // This method is already synchronized on our lock.
    private static void maybeSendImpressions(@NonNull final SharedPreferences preferences,
                                             @NonNull final Set<String> impressions) {
        if (sIsSending) return;
        final long now = System.currentTimeMillis();
        final long then = preferences.getLong(RECORD_KEY_TIME, 0L);
        if (now > then + REPORT_TIME_MILLIS || impressions.size() >= REPORT_MAX_SIZE) {
            JSONObject payload;
            try {
                JSONArray array = new JSONArray();
                for (String impression : impressions) {
                    array.put(new JSONObject(impression));
                }
                payload = new JSONObject();
                payload.put("impressions", array);
                BranchSearchInterface.fillPayload(payload,
                        BranchSearch.getInstance().getBranchConfiguration());
            } catch (JSONException e) {
                return;
            }

            // We're ready to upload.
            sIsSending = true;
            sSender.executePost(REPORT_URL, payload, new IURLConnectionEvents() {
                @Override
                public void onResult(@NonNull JSONObject response) {
                    // This callback is called on a background thread, so we need a lock
                    // to synchronize access.
                    boolean success = !(response instanceof BranchSearchError);
                    synchronized (sSendLock) {
                        sIsSending = false;
                        if (success) {
                            // Remove the sent values from preferences so we don't send twice.
                            Set<String> set = preferences.getStringSet(RECORD_KEY, null);
                            if (set == null) set = new HashSet<>();
                            set.removeAll(impressions);
                            preferences.edit()
                                    .putStringSet(RECORD_KEY, set)
                                    .putLong(RECORD_KEY_TIME, now)
                                    .apply();
                        } else {
                            // Nothing to do. The preference key already contains all impressions
                            // (those we tried to send, and any other that came while sending).
                        }
                    }
                }
            });
        }
    }
}
