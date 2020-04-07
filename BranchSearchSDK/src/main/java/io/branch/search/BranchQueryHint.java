package io.branch.search;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Represents a single query hint result.
 */
public class BranchQueryHint implements Parcelable {
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

    /**
     * Returns a search request that will search for this hint's query.
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

    public final static Creator<BranchQueryHint> CREATOR = new Creator<BranchQueryHint>() {
        @Override
        public BranchQueryHint createFromParcel(Parcel source) {
            //noinspection ConstantConditions
            return new BranchQueryHint(source.readString());
        }

        @Override
        public BranchQueryHint[] newArray(int size) {
            return new BranchQueryHint[size];
        }
    };
}
