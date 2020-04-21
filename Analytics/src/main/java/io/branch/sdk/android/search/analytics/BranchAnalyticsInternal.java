package io.branch.sdk.android.search.analytics;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static io.branch.sdk.android.search.analytics.BranchAnalytics.LOGTAG;

// todo register receiver for ACTION_SHUTDOWN intent to clean up (https://developer.android.com/reference/android/content/Intent.html#ACTION_SHUTDOWN)

class BranchAnalyticsInternal implements LifecycleObserver {
    private static BranchAnalyticsInternal instance;

//    // clear and reuse per app session (from coming to foreground to going to background)
//    private List search_request_ids = Collections.synchronizedList(new LinkedList<String>());
//    private List hints_request_ids = Collections.synchronizedList(new LinkedList<String>());
//    private List autosuggest_request_ids = Collections.synchronizedList(new LinkedList<String>());

    // clear and reuse per API request
    private List<JSONObject> clicks = Collections.synchronizedList(new LinkedList<JSONObject>());
    private List<JSONObject> impressions = Collections.synchronizedList(new LinkedList<JSONObject>());

    private HashMap<String, List<JSONObject>> clicksPerCategory = new HashMap<String, List<JSONObject>>();// e.g. "hints" gives you hints_clicks
    private HashMap<String, List<JSONObject>> impressionsPerCategory = new HashMap<String, List<JSONObject>>();// e.g. "hints" gives you hints_impressions
    private HashMap<String, List<JSONObject>> locksPerCategory = new HashMap<String, List<JSONObject>>();// e.g. "hints_clicks" gives you hints_lock

    /**
     * 'sessionBatch' stores the following:
     *
     * <"search_request_ids", search_request_ids>
     * <"hints_request_ids", hints_request_ids>
     * <"auto_suggest_request_ids", autosuggest_request_ids>
     * <request_id + "_clicks", clicks>
     * <request_id + "_impressions", impressions>
     */
    private HashMap compositeClicksAndImpressionsBatch = new HashMap<String, List<?>>();

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMoveToForeground() {
        Log.d(LOGTAG, "Returning to foreground…");
        // startNewAnalyticsWindow();
    }

    // Note, this is invoked with 700 millisecond delay to ensure app is actually going to
    // background, and that it's not just device rotation change.
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground() {
        Log.d(LOGTAG, "Moving to background…");
        // uploadSessionAnalytics(sessionBatch);
    }

    private void startNewAnalyticsWindow() {

    }

    private void uploadSessionAnalytics(HashMap sessionData) {
        JSONObject payload = formatPayload();
    }

    void registerClickEvent(@NonNull JSONObject click, @Nullable String category) {
        if (category == null) {
            // write to default clicks
            clicks.add(click);
        } else {
            List<JSONObject> categoryClicks = clicksPerCategory.get(category);
            if (categoryClicks == null) {
                categoryClicks = new LinkedList<JSONObject>();
            }

            categoryClicks.add(click);
            clicksPerCategory.put(category, categoryClicks);
        }
    }

    void registerImpressionEvent(@NonNull JSONObject impression, @Nullable String impressionCategory) {
        if (impressionCategory == null) {
            // write to default impressions
        } else {
            // get the list from impressionsPerCategory and update it
        }
    }

    void registerCustomEvent(@NonNull String key, @NonNull JSONObject customEvent) {

    }

    @NonNull JSONObject formatPayload() {

        return new JSONObject();
    }

    @NonNull
    static BranchAnalyticsInternal getInstance() {
        if (instance == null) {
            instance = new BranchAnalyticsInternal();
        }
        return instance;
    }
}
