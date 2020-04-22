package io.branch.sdk.android.search.analytics;

import android.app.Application;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Based on https://www.notion.so/branchdisco/SDK-side-Analytics-Module-spec-ff2b69a0438649d287a794b7298a5f10
 */
public class BranchAnalytics {
    static final String LOGTAG = "BranchAnalytics";

    private static WeakReference<Context> appContext;
    private static BranchAnalyticsInternal analyticsInternal;

    /**
     * Initialize Analytics in App.onCreate()
     */
    public static void init(Application application) {
        appContext = new WeakReference<Context>(application);
        analyticsInternal = new BranchAnalyticsInternal();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(analyticsInternal);
    }

    /**
     * Use APIs registerClickEvent and registerImpressionEvent to register default clicks and events,
     * or split them up in the payload by "clickCategory" and "impressionCategory"
     */
    public static void registerClickEvent(@NonNull JSONObject click) {
        registerClickEvent(click, null);
    }

    public static void registerClickEvent(@NonNull JSONObject click, @Nullable String clickCategory) {
        analyticsInternal.registerClickEvent(click, clickCategory);
    }

    public static void registerImpressionEvent(@NonNull JSONObject impression) {
        registerImpressionEvent(impression, null);
    }

    public static void registerImpressionEvent(@NonNull JSONObject impression, @Nullable String impressionCategory) {
        analyticsInternal.registerImpressionEvent(impression, impressionCategory);
    }

    /**
     * `recordXXX` APIs add the XXX entity to top level of the payload and are treated as records of
     * user behavior, thus `isEmptySession()` will return false if there is even a single record or user behavior
     */
    public static void recordObject(@NonNull String key, @NonNull JSONObject customEvent) {
        analyticsInternal.recordObject(key, customEvent);
    }

    public static void recordString(@NonNull String key, @NonNull String customString) {
        analyticsInternal.recordString(key, customString);
    }

    public static void recordInt(@NonNull String key, Integer customInt) {
        analyticsInternal.recordInt(key, customInt);
    }

    public static void recordDouble(@NonNull String key, @NonNull Double customDouble) {
        analyticsInternal.recordDouble(key, customDouble);
    }

    /**
     * `addXXX` APIs add the XXX entity to top level of the payload. These values are treated as static
     * and not related to the user behavior. If static data is the only data passed to analytics during
     * this session, we will treat it as an empty session and will not make the upload to the servers.
     */
    void addObject(@NonNull String key, @NonNull JSONObject staticObject) {
        analyticsInternal.addObject(key, staticObject);// (e.g. 'device_info', 'sdk_configuration')
    }

    void addString(@NonNull String key, @NonNull String staticString) {
        analyticsInternal.addString(key, staticString);// (e.g. 'branch_key')
    }

    void addInt(@NonNull String key, @NonNull Integer staticInt) {
        analyticsInternal.addInt(key, staticInt);// (e.g. ??)
    }

    void addDouble(@NonNull String key, @NonNull Double staticDouble) {
        analyticsInternal.addDouble(key, staticDouble);// (e.g. ???)
    }

    /**
     * Get the current state of analytics batch
     */
    public static JSONObject getAnalyticsData() {
        return analyticsInternal.formatPayload();
    }

    /**
     * Get analytics window id, so it can added to API requests for analytics data matching on the backend
     */
    public static String getAnalyticsWindowId() {
        return analyticsInternal.sessionId;
    }
}
