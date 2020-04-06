package io.branch.search;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Request model for query hint requests.
 */
public class BranchQueryHintRequest extends BranchDiscoveryRequest<BranchQueryHintRequest> {

    static final String KEY_MAX_RESULTS = "num";

    private int maxResults = 0;

    /**
     * Factory Method to create a new BranchQueryHintRequest.
     * @return a new BranchQueryHintRequest.
     */
    @NonNull
    public static BranchQueryHintRequest create() {
        return new BranchQueryHintRequest();
    }

    private BranchQueryHintRequest() {}

    /**
     * Sets the maximum number of hints that the query should return.
     * @param maxResults max results
     * @return this for chaining
     */
    @SuppressWarnings("UnusedReturnValue")
    public BranchQueryHintRequest setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    @NonNull
    @Override
    JSONObject toJson() {
        JSONObject object = super.toJson();
        try {
            if (maxResults > 0) {
                object.putOpt(KEY_MAX_RESULTS, maxResults);
            }
        } catch (JSONException ignore) {}
        return object;
    }
}
