package io.branch.search;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Represents a single auto suggest result.
 */
public class BranchAutoSuggestion implements Parcelable {
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

    /**
     * Returns a search request that will search for this suggestion's query.
     * @return a new search request
     */
    @NonNull
    public BranchSearchRequest toSearchRequest() {
        return BranchSearchRequest.create(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(query);
    }

    public final static Creator<BranchAutoSuggestion> CREATOR = new Creator<BranchAutoSuggestion>() {
        @Override
        public BranchAutoSuggestion createFromParcel(Parcel source) {
            //noinspection ConstantConditions
            return new BranchAutoSuggestion(source.readString());
        }

        @Override
        public BranchAutoSuggestion[] newArray(int size) {
            return new BranchAutoSuggestion[size];
        }
    };
}
