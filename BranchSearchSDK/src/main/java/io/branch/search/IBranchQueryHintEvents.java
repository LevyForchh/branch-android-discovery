package io.branch.search;

import android.support.annotation.NonNull;

/**
 * Callback for Query Hint API events. Should be passed to
 * {@link BranchSearch#queryHint(BranchQueryHintRequest, IBranchQueryHintEvents)}.
 */
public interface IBranchQueryHintEvents {
    /**
     * Called when query hint results are available
     * @param result an object containing the results
     */
    void onBranchQueryHintResult();

    /**
     * Called when there was an error processing this query hint request.
     * @param error {@link BranchSearchError} object with error code and detailed message
     */
    void onBranchQueryHintError(@NonNull BranchSearchError error);
}
