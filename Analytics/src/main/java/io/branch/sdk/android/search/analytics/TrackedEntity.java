package io.branch.sdk.android.search.analytics;

import org.json.JSONObject;

public interface TrackedEntity {
    JSONObject getImpressionJson();

    JSONObject getClickJson();

    String getAPI();
}
