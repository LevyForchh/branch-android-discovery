package io.branch.search;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import io.branch.sdk.android.search.analytics.TrackedEntity;

import static io.branch.search.Defines.AnalyticsJsonKey.Autosuggest;
import static io.branch.search.Defines.AnalyticsJsonKey.Autosuggestion;
import static io.branch.search.Defines.AnalyticsJsonKey.Hint;
import static io.branch.search.Defines.AnalyticsJsonKey.RequestId;

/**
 * Represents a single auto suggest result.
 */
public class BranchAutoSuggestion implements Parcelable, TrackedEntity {
    private final String query;
    private final String requestId;

    BranchAutoSuggestion(@NonNull String query, @NonNull String requestId) {
        this.query = query;
        this.requestId = requestId;
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

    @NonNull
    public String getRequestId() {
        return requestId;
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
        dest.writeString(requestId);
    }

    public final static Creator<BranchAutoSuggestion> CREATOR = new Creator<BranchAutoSuggestion>() {
        @Override
        public BranchAutoSuggestion createFromParcel(Parcel source) {
            //noinspection ConstantConditions
            String query = source.readString();
            String requestId = source.readString();
            return new BranchAutoSuggestion(query, requestId);
        }

        @Override
        public BranchAutoSuggestion[] newArray(int size) {
            return new BranchAutoSuggestion[size];
        }
    };

    @Override
    public JSONObject getImpressionJson() {
        JSONObject impression = new JSONObject();
        try {
            impression.putOpt(Autosuggestion.getKey(), getQuery());
            impression.putOpt(RequestId.getKey(), getRequestId());
        } catch (JSONException ignored) {}
        return impression;
    }

    @Override
    public JSONObject getClickJson() {
        JSONObject click = new JSONObject();
        try {
            click.putOpt(Autosuggestion.getKey(), getQuery());
            click.putOpt(RequestId.getKey(), getRequestId());
        } catch (JSONException ignored) {}
        return click;
    }

    @Override
    public String getAPI() {
        return Autosuggest.getKey();
    }
}
