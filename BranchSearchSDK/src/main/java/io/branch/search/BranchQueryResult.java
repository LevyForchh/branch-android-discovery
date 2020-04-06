package io.branch.search;

import android.os.Parcel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for representing a a query result.
 * @deprecated please use {@link BranchQueryHintResult} for query hints and
 *             {@link BranchAutoSuggestResult} for autosuggest
 */
@Deprecated
public class BranchQueryResult {
    private static final String RESULTS_KEY = "results";

    final List<String> queryResults = new ArrayList<>();

    BranchQueryResult() {
    }

    public List<String> getQueryResults() {
        return queryResults;
    }

    static BranchQueryResult createFromJson(JSONObject jsonObject) {
        BranchQueryResult result = new BranchQueryResult();

        try {
            JSONArray jsonArray = jsonObject.optJSONArray(RESULTS_KEY);
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    result.queryResults.add(jsonArray.getString(i));
                }
            }
        } catch (JSONException e) {
        }

        return result;
    }
}
