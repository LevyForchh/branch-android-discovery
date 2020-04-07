package io.branch.search;

import android.support.annotation.NonNull;

/**
 * Callback for Search API events. Should be passed to
 * {@link BranchSearch#query(BranchSearchRequest, IBranchSearchEvents)}.
 */
public interface IBranchSearchEvents {
    /**
     * Called when there is a Branch search result available
     * @param result {@link BranchSearchResult} object containing the search result
     */
    void onBranchSearchResult(@NonNull BranchSearchResult result);

    /**
     * Called when there is an error occurred while searching operation with Branch search SDK
     * @param error {@link BranchSearchError} object with error code and detailed message
     */
    void onBranchSearchError(@NonNull BranchSearchError error);
}
