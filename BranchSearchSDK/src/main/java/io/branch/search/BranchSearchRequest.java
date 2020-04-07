package io.branch.search;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Request model for Branch Search.
 */
public class BranchSearchRequest extends BranchDiscoveryRequest<BranchSearchRequest> {

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
    private int maxAppResults = 0;
    private int maxContentPerAppResults = 0;

    /**
     * Deprecated method. Please use {@link #create(String)} instead.
     * @deprecated please use {@link #create(String)} instead
     * @return a new BranchSearchRequest
     */
    @Deprecated
    @NonNull
    public static BranchSearchRequest Create(@NonNull String query) {
        return create(query);
    }

    /**
     * Factory method to create a new BranchSearchRequest.
     * @param query Query String to use
     * @return a new BranchSearchRequest
     */
    @NonNull
    public static BranchSearchRequest create(@NonNull String query) {
        return new BranchSearchRequest(query);
    }

    /**
     * Factory method to create a new BranchSearchRequest.
     * @param hint the hint that triggered this query
     * @return a new BranchSearchRequest
     */
    @NonNull
    public static BranchSearchRequest create(@NonNull BranchQueryHint hint) {
        return create(hint.getQuery()).setQuerySource(BranchQuerySource.QUERY_HINT_RESULTS);
    }

    /**
     * Factory method to create a new BranchSearchRequest.
     * @param suggestion the suggestion that triggered this query
     * @return a new BranchSearchRequest
     */
    @NonNull
    public static BranchSearchRequest create(@NonNull BranchAutoSuggestion suggestion) {
        return create(suggestion.getQuery()).setQuerySource(BranchQuerySource.AUTOSUGGEST_RESULTS);
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
    public String getQuery() {
        return query;
    }

    @NonNull
    @Override
    JSONObject toJson() {
        JSONObject object = super.toJson();
        try {
            if (maxAppResults > 0) {
                object.putOpt(KEY_LIMIT_APP_RESULTS, maxAppResults);
            }
            if (maxContentPerAppResults > 0) {
                object.putOpt(KEY_LIMIT_LINK_RESULTS, maxContentPerAppResults);
            }
            object.putOpt(KEY_USER_QUERY, query);
            if (doNotModifyQuery) {
                object.putOpt(KEY_DO_NOT_MODIFY, true);
            }
            object.putOpt(KEY_QUERY_SOURCE, querySource);
        } catch (JSONException ignore) {}
        return object;
    }
}
