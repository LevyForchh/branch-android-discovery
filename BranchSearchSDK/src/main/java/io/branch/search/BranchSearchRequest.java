package io.branch.search;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Request model for Branch Search.
 */
public class BranchSearchRequest extends BranchDiscoveryRequest<BranchSearchRequest> {
    // Default result limits
    private static final int MAX_APP_RESULT = 5;
    private static final int MAX_CONTENT_PER_APP_RESULT = 5;

    static final String KEY_LIMIT_APP_RESULTS = "limit_app_results";
    static final String KEY_LIMIT_LINK_RESULTS = "limit_link_results";
    static final String KEY_USER_QUERY = "user_query";
    static final String KEY_DO_NOT_MODIFY = "do_not_modify";
    static final String KEY_QUERY_SOURCE = "query_source";

    // User query string
    @NonNull
    private final String query;

    // Flag to send to the server to indicate the query is exactly what they want.
    private boolean doNotModifyQuery;

    // Source of the query
    private BranchQuerySource querySource = BranchQuerySource.UNSPECIFIED;

    // Result limit params
    private int maxAppResults = MAX_APP_RESULT;
    private int maxContentPerAppResults = MAX_CONTENT_PER_APP_RESULT;

    /**
     * Factory Method to create a new BranchSearchRequest.
     * @param query Query String to use
     * @return a new BranchSearchRequest.
     */
    @NonNull
    public static BranchSearchRequest create(@NonNull String query) {
        return new BranchSearchRequest(query);
    }

    private BranchSearchRequest(@NonNull String query) {
        super();
        this.query = query;
    }

    /**
     * Disable Query Modifications.
     * This is a flag to suggest that Branch should not internally modify a query when logic dictates
     * the user intended a query other than what they actually typed. For example, because of a typo.
     * @return this BranchSearchRequest
     */
    @NonNull
    @SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
    public BranchSearchRequest disableQueryModification() {
        this.doNotModifyQuery = true;
        return this;
    }

    @NonNull
    @SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
    public BranchSearchRequest setMaxContentPerAppResults(int maxContentPerAppResults) {
        this.maxContentPerAppResults = maxContentPerAppResults;
        return this;
    }

    @NonNull
    @SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
    public BranchSearchRequest setMaxAppResults(int maxAppResults) {
        this.maxAppResults = maxAppResults;
        return this;
    }

    /**
     * Should be called to notify that the query in this request originated from the
     * results of another Branch SDK call.
     * @param querySource the source
     * @return this for chaining
     */
    @SuppressWarnings("WeakerAccess")
    @NonNull
    public BranchSearchRequest setQuerySource(@NonNull BranchQuerySource querySource) {
        this.querySource = querySource;
        return this;
    }

    @NonNull
    JSONObject toJson() {
        JSONObject object = new JSONObject();
        super.toJson(object);
        try {
            object.putOpt(KEY_LIMIT_APP_RESULTS, maxAppResults);
            object.putOpt(KEY_LIMIT_LINK_RESULTS, maxContentPerAppResults);
            object.putOpt(KEY_USER_QUERY, query);
            if (doNotModifyQuery) {
                object.putOpt(KEY_DO_NOT_MODIFY, true);
            }
            object.putOpt(KEY_QUERY_SOURCE, querySource);
        } catch (JSONException ignore) {}
        return object;
    }

    @NonNull
    public String getQuery() {
        return query;
    }
}
