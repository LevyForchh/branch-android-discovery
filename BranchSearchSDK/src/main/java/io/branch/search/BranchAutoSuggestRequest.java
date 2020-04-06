package io.branch.search;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Request model for auto suggest requests.
 */
public class BranchAutoSuggestRequest extends BranchDiscoveryRequest<BranchAutoSuggestRequest> {

    static final String KEY_USER_QUERY = "user_query";

    @NonNull
    private final String query;

    /**
     * Factory Method to create a new BranchAutoSuggestRequest.
     * @return a new BranchAutoSuggestRequest.
     */
    @NonNull
    public static BranchAutoSuggestRequest create(@NonNull String query) {
        return new BranchAutoSuggestRequest(query);
    }

    private BranchAutoSuggestRequest(@NonNull String query) {
        this.query = query;
    }

    @NonNull
    public String getQuery() {
        return query;
    }

    @NonNull
    @Override
    JSONObject toJson() {
        JSONObject object = super.toJson();
        try {
            object.putOpt(KEY_USER_QUERY, query);
        } catch (JSONException ignore) {}
        return object;
    }
}
