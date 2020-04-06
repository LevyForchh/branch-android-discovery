package io.branch.search;

import android.support.annotation.NonNull;

/**
 * Represents a single query hint result.
 */
public class BranchQueryHint {
    private final String query;

    BranchQueryHint(@NonNull String query) {
        this.query = query;
    }

    @NonNull
    public String getQuery() {
        return query;
    }

    @NonNull
    @Override
    public String toString() {
        return query;
    }
}
