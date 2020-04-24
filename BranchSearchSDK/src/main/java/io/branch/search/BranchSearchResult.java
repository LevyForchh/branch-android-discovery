package io.branch.search;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Branch Search results.
 */
public class BranchSearchResult {

    private static final String KEY_REQUEST_ID = "request_id";
    private static final String KEY_RESULTS = "results";
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_CORRECTED_QUERY = "search_query_string";

    private final BranchSearchRequest query;
    private final String correctedQuery;
    private final List<BranchAppResult> results;
    private final String requestId;

    private BranchSearchResult(@NonNull BranchSearchRequest query,
                               @Nullable String correctedQuery,
                               @NonNull List<BranchAppResult> results,
                               @NonNull String requestId) {
        this.query = query;
        this.correctedQuery = correctedQuery;
        this.results = results;
        this.requestId = requestId;
    }

    /**
     * @return the original Branch search query.
     */
    @NonNull
    public BranchSearchRequest getBranchSearchRequest() {
        return query;
    }

    /**
     * @return the corrected Branch search query.
     */
    @Nullable
    public String getCorrectedQuery() { return correctedQuery; }

    /**
     * @return a list of {@link BranchAppResult}.
     */
    @NonNull
    public List<BranchAppResult> getResults() {
        return this.results;
    }

    /**
     * Parses a {@link BranchSearchResult} from JSON object.
     * @param query original query
     * @param json json object
     * @return a result
     */
    @NonNull
    static BranchSearchResult createFromJson(@NonNull BranchSearchRequest query,
                                             @NonNull JSONObject json) {
        String correctedQuery = null;
        if (json.has(KEY_CORRECTED_QUERY)) {
            correctedQuery = json.optString(KEY_CORRECTED_QUERY);
        }
        @NonNull String requestId = "";
        if (json.has(KEY_REQUEST_ID)) {
            requestId = json.optString(KEY_REQUEST_ID);
        }
        List<BranchAppResult> results = new ArrayList<>();
        if (json.optBoolean(KEY_SUCCESS)) {
            JSONArray resultsJson = json.optJSONArray(KEY_RESULTS);
            if (resultsJson != null) {
                for (int i = 0; i < resultsJson.length(); i++) {
                    JSONObject resultJson = resultsJson.optJSONObject(i);
                    if (resultJson == null) continue;

                    try {
                        // add request id to individual BranchAppResult json
                        resultJson.putOpt(KEY_REQUEST_ID, requestId);
                    } catch (JSONException ignored) {}

                    BranchAppResult result = BranchAppResult.createFromJson(resultJson);
                    if (result != null) {
                        results.add(result);
                    }
                }
            }
        }
        return new BranchSearchResult(query, correctedQuery, results, requestId);
    }
}