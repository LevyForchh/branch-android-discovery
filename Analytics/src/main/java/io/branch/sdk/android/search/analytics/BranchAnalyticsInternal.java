package io.branch.sdk.android.search.analytics;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static io.branch.sdk.android.search.analytics.BranchAnalytics.LOGTAG;

// todo register receiver for ACTION_SHUTDOWN intent to clean up (https://developer.android.com/reference/android/content/Intent.html#ACTION_SHUTDOWN)
class BranchAnalyticsInternal implements LifecycleObserver {
    private static final String BNC_ANALYTICS_NO_VAL = "BNC_ANALYTICS_NO_VAL";

    @NonNull String sessionId = BNC_ANALYTICS_NO_VAL;

    // clicks and impressions
    private List<JSONObject> clicks = Collections.synchronizedList(new LinkedList<JSONObject>());
    private List<JSONObject> impressions = Collections.synchronizedList(new LinkedList<JSONObject>());
    private final HashMap<String, List<JSONObject>> clicksPerCategory = new HashMap<>();// e.g. "hints" gives you hints_clicks
    private final HashMap<String, List<JSONObject>> impressionsPerCategory = new HashMap<>();// e.g. "hints" gives you hints_impressions

    // custom values
    private final ConcurrentHashMap<String, JSONObject> customObjects = new ConcurrentHashMap<>();// e.g. ???
    private final ConcurrentHashMap<String, String> customStrings = new ConcurrentHashMap<>();// e.g. ???
    private final ConcurrentHashMap<String, Integer> customInts = new ConcurrentHashMap<>();// e.g. ???
    private final ConcurrentHashMap<String, Double> customDoubles = new ConcurrentHashMap<>();// e.g. ???

    // static values
    private final ConcurrentHashMap<String, JSONObject> staticObjects = new ConcurrentHashMap<>();// e.g. "device_info", "sdk_config"
    private final ConcurrentHashMap<String, String> staticStrings = new ConcurrentHashMap<>();// e.g. "branch_key"
    private final ConcurrentHashMap<String, Integer> staticInts = new ConcurrentHashMap<>();// e.g. "empty_sessions"
    private final ConcurrentHashMap<String, Double> staticDoubles = new ConcurrentHashMap<>();// e.g. ???

    private int emptySessionCount = 0;

    /**
     * This is where we start collecting analytics
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMoveToForeground() {
        Log.d(LOGTAG, "Returning to foreground");
        sessionId =  UUID.randomUUID().toString();
    }

    /**
     * This is where we stop collecting analytics and start uploading them to the server
     * Note, this method is invoked with 700 millisecond delay to ensure app is actually going to
     * background, and that it's not just device rotation change.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground() {
        Log.d(LOGTAG, "Moving to background");
        if (!isEmptySession()) {
            JSONObject payload = formatPayload();
            startUpload(payload);
            emptySessionCount = 0;
        } else {
            emptySessionCount++;
        }
        // cleanup
        sessionId = BNC_ANALYTICS_NO_VAL;
    }

    private boolean isEmptySession() {
        // we ignore static payload values
        boolean noClicksOrImpressions = clicks.isEmpty() && impressions.isEmpty() && clicksPerCategory.isEmpty() && impressionsPerCategory.isEmpty();
        boolean noCustomValues = customObjects.isEmpty() && customStrings.isEmpty() && customInts.isEmpty() && customDoubles.isEmpty();
        return noClicksOrImpressions && noCustomValues;
    }

    private void startUpload(JSONObject payload) {

    }

    void registerClickEvent(@NonNull JSONObject click, @Nullable String category) {
        if (TextUtils.isEmpty(category)) {
            // write to default clicks
            clicks.add(click);
        } else {
            synchronized (clicksPerCategory) {
                List<JSONObject> categoryClicks = clicksPerCategory.get(category);
                if (categoryClicks == null) {
                    categoryClicks = new LinkedList<JSONObject>();
                }
                categoryClicks.add(click);
                clicksPerCategory.put(category, categoryClicks);
            }
        }
    }

    void registerImpressionEvent(@NonNull JSONObject impression, @Nullable String category) {
        if (TextUtils.isEmpty(category)) {
            // write to default impressions
            impressions.add(impression);
        } else {
            synchronized (impressionsPerCategory) {
                List<JSONObject> categoryImpressions = impressionsPerCategory.get(category);
                if (categoryImpressions == null) {
                    categoryImpressions = new LinkedList<JSONObject>();
                }
                categoryImpressions.add(impression);
                impressionsPerCategory.put(category, categoryImpressions);
            }
        }
    }

    @NonNull JSONObject formatPayload() {

        JSONObject payload = new JSONObject();
        try {
            payload.putOpt("analytics_window_id", sessionId);
            payload.putOpt("empty_sessions", emptySessionCount);

            // top level: static data not related to user actions
            for (Map.Entry<String, String> customStringEntry : customStrings.entrySet()) {
                payload.putOpt(customStringEntry.getKey(), customStringEntry.getValue());
            }
            for (Map.Entry<String, Integer> customIntEntry : customInts.entrySet()) {
                payload.putOpt(customIntEntry.getKey(), customIntEntry.getValue());
            }
            for (Map.Entry<String, Double> customDoubleEntry : customDoubles.entrySet()) {
                payload.putOpt(customDoubleEntry.getKey(), customDoubleEntry.getValue());
            }
            for (Map.Entry<String, JSONObject> customObjectEntry : customObjects.entrySet()) {
                payload.putOpt(customObjectEntry.getKey(), customObjectEntry.getValue());
            }

            // top level: records of unique and uncommon user actions
            for (Map.Entry<String, String> customStringEntry : customStrings.entrySet()) {
                payload.putOpt(customStringEntry.getKey(), customStringEntry.getValue());
            }
            for (Map.Entry<String, Integer> customIntEntry : customInts.entrySet()) {
                payload.putOpt(customIntEntry.getKey(), customIntEntry.getValue());
            }
            for (Map.Entry<String, Double> customDoubleEntry : customDoubles.entrySet()) {
                payload.putOpt(customDoubleEntry.getKey(), customDoubleEntry.getValue());
            }
            for (Map.Entry<String, JSONObject> customObjectEntry : customObjects.entrySet()) {
                payload.putOpt(customObjectEntry.getKey(), customObjectEntry.getValue());
            }

            // 2nd level: records of common user actions that can will be grouped together
            for (Map.Entry<String, List<JSONObject>> categoryClicksEntry : clicksPerCategory.entrySet()) {
                payload.putOpt(categoryClicksEntry.getKey() + "_clicks", new JSONArray(categoryClicksEntry.getValue()));
            }
            for (Map.Entry<String, List<JSONObject>> categoryImpressionsEntry : impressionsPerCategory.entrySet()) {
                payload.putOpt(categoryImpressionsEntry.getKey() + "_impressions", new JSONArray(categoryImpressionsEntry.getValue()));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return payload;
    }

    /**
     * `recordXXX` APIs add the XXX entity to top level of the payload and are treated as records of
     * user behavior, thus `isEmptySession()` will return false if there is even a single record or user behavior
     */
    void recordObject(@NonNull String key, @NonNull JSONObject customObject) {
        customObjects.put(key, customObject);
    }

    void recordString(@NonNull String key, @NonNull String customString) {
        customStrings.put(key, customString);
    }

    void recordInt(@NonNull String key, @NonNull Integer customInt) {
        customInts.put(key, customInt);
    }

    void recordDouble(@NonNull String key, @NonNull Double customDouble) {
        customDoubles.put(key, customDouble);
    }

    /**
     * `addXXX` APIs add the XXX entity to top level of the payload. These values are treated as static
     * and not related to the user behavior. If static data is the only data recorded during this session,
     * we will treat it as an empty session and will not make the upload to the servers.
     */
    void addObject(@NonNull String key, @NonNull JSONObject staticObject) {
        staticObjects.put(key, staticObject);
    }

    void addString(@NonNull String key, @NonNull String staticString) {
        staticStrings.put(key, staticString);
    }

    void addInt(@NonNull String key, @NonNull Integer staticInt) {
        staticInts.put(key, staticInt);
    }

    void addDouble(@NonNull String key, @NonNull Double staticDouble) {
        staticDoubles.put(key, staticDouble);
    }
}
