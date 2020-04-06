package io.branch.search;

import android.os.Parcel;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents results of an auto suggest query started from
 * {@link BranchSearch#autoSuggest(BranchAutoSuggestRequest, IBranchAutoSuggestEvents)}.
 */
public class BranchAutoSuggestResult {
    private static final String KEY_RESULTS = "results";

    private final List<BranchAutoSuggestion> suggestions;

    private BranchAutoSuggestResult(@NonNull List<BranchAutoSuggestion> suggestions) {
        this.suggestions = suggestions;
    }

    @NonNull
    public List<BranchAutoSuggestion> getSuggestions() {
        return suggestions;
    }

    @NonNull
    static BranchAutoSuggestResult createFromJson(@NonNull JSONObject jsonObject) {
        List<BranchAutoSuggestion> suggestions = new ArrayList<>();
        try {
            JSONArray jsonArray = jsonObject.optJSONArray(KEY_RESULTS);
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    suggestions.add(new BranchAutoSuggestion(jsonArray.getString(i)));
                }
            }
        } catch (JSONException ignore) { }
        return new BranchAutoSuggestResult(suggestions);
    }
}
