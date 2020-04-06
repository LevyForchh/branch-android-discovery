package io.branch.search;

import android.support.annotation.NonNull;

import org.json.JSONObject;

/**
 * Request model for query hint requests.
 */
public class BranchQueryHintRequest extends BranchDiscoveryRequest<BranchQueryHintRequest> {

    /**
     * Factory Method to create a new BranchQueryHintRequest.
     * @return a new BranchQueryHintRequest.
     */
    @NonNull
    public static BranchQueryHintRequest create() {
        return new BranchQueryHintRequest();
    }

    @NonNull
    JSONObject toJson() {
        JSONObject object = new JSONObject();
        super.toJson(object);
        return object;
    }
}
