package io.branch.search;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Branch Search results.
 */
public class BranchSearchResult {
    private final BranchSearchRequest query;
    private final String correctedQuery;
    final List<BranchAppResult> results = new ArrayList<>();

    BranchSearchResult(@NonNull BranchSearchRequest query, @Nullable String correctedQuery) {
        this.query = query;
        this.correctedQuery = correctedQuery;
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
}