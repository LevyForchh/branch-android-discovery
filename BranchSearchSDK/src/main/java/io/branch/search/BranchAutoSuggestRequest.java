package io.branch.search;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Request model for auto suggest requests.
 */
public class BranchAutoSuggestRequest extends BranchDiscoveryRequest<BranchAutoSuggestRequest> {

    static final String KEY_USER_QUERY = "user_query";
    static final String KEY_MAX_RESULTS = "limit";

    private int maxResults = 0;
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

    /**
     * Sets the maximum number of hints that the query should return.
     * @param maxResults max results
     * @return this for chaining
     */
    @SuppressWarnings("UnusedReturnValue")
    public BranchAutoSuggestRequest setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
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
            if (maxResults > 0) {
                object.putOpt(KEY_MAX_RESULTS, maxResults);
            }
        } catch (JSONException ignore) {}
        return object;
    }
}
