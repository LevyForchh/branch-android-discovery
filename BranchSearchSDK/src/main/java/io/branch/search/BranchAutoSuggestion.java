package io.branch.search;

import android.support.annotation.NonNull;

/**
 * Represents a single auto suggest result.
 */
public class BranchAutoSuggestion {
    private final String query;

    BranchAutoSuggestion(@NonNull String query) {
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
