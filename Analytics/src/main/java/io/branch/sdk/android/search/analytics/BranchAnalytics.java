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
     * Register custom events such as device_info or sdk_configuration
     */
    public static void registerCustomEvent(@NonNull String key, @NonNull JSONObject customEvent) {
        analyticsInternal.registerCustomEvent(key, customEvent);
    }

    /**
     * Get the current state of analytics batch
     */
    public static JSONObject getAnalyticsData() {
        return analyticsInternal.formatPayload();
    }
}
