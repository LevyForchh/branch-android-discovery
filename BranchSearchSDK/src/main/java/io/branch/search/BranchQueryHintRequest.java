package io.branch.search;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Request model for query hint requests.
 */
public class BranchQueryHintRequest extends BranchDiscoveryRequest<BranchQueryHintRequest> {

    static final String KEY_MAX_RESULTS = "max_results";

    private int maxResults = 0;

    /**
     * Factory Method to create a new BranchQueryHintRequest.
     * @return a new BranchQueryHintRequest.
     */
    @NonNull
    public static BranchQueryHintRequest create() {
        return new BranchQueryHintRequest();
    }

    /**
     * Sets the maximum number of hints that the query should return.
     * @param maxResults max results
     * @return this for chaining
     */
    @SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
    public BranchQueryHintRequest setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    @NonNull
    JSONObject toJson() {
        JSONObject object = new JSONObject();
        super.toJson(object);
        try {
            if (maxResults > 0) {
                object.putOpt(KEY_MAX_RESULTS, maxResults);
            }
        } catch (JSONException ignore) {}
        return object;
    }
}
