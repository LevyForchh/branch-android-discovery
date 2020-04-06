package io.branch.search;

import android.support.annotation.NonNull;

/**
 * Callback for Auto Suggest API events. Should be passed to
 * {@link BranchSearch#autoSuggest(BranchAutoSuggestRequest, IBranchAutoSuggestEvents)}.
 */
public interface IBranchAutoSuggestEvents {
    /**
     * Called when auto suggest results are available
     * @param result an object containing the results
     */
    void onBranchAutoSuggestResult(@NonNull BranchAutoSuggestResult result);

    /**
     * Called when there was an error processing this auto suggest request.
     * @param error {@link BranchSearchError} object with error code and detailed message
     */
    void onBranchAutoSuggestError(@NonNull BranchSearchError error);
}
