package io.branch.search;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import io.branch.sdk.android.search.analytics.TrackedEntity;

import static io.branch.search.Defines.AnalyticsJsonKey.Hint;
import static io.branch.search.Defines.AnalyticsJsonKey.Hints;
import static io.branch.search.Defines.AnalyticsJsonKey.RequestId;

/**
 * Represents a single query hint result.
 */
public class BranchQueryHint implements Parcelable, TrackedEntity {
    private final String query;
    private final String requestId;

    BranchQueryHint(@NonNull String query, @NonNull String requestId) {
        this.query = query;
        this.requestId = requestId;
    }

    @NonNull
    public String getQuery() {
        return query;
    }

    @NonNull
    public String getRequestId() {
        return requestId;
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
        dest.writeString(requestId);
    }

    public final static Creator<BranchQueryHint> CREATOR = new Creator<BranchQueryHint>() {
        @Override
        public BranchQueryHint createFromParcel(Parcel source) {
            //noinspection ConstantConditions
            String hint = source.readString();
            String requestId = source.readString();
            return new BranchQueryHint(hint, requestId);
        }

        @Override
        public BranchQueryHint[] newArray(int size) {
            return new BranchQueryHint[size];
        }
    };

    @Override
    public JSONObject getImpressionJson() {
        JSONObject impression = new JSONObject();
        try {
            impression.putOpt(Hint.getKey(), getQuery());
            impression.putOpt(RequestId.getKey(), getRequestId());
        } catch (JSONException ignored) {}
        return impression;
    }

    @Override
    public JSONObject getClickJson() {
        JSONObject click = new JSONObject();
        try {
            click.putOpt(Hint.getKey(), getQuery());
            click.putOpt(RequestId.getKey(), getRequestId());
        } catch (JSONException ignored) {}
        return click;
    }

    @Override
    public String getAPI() {
        return Hints.getKey();
    }
}
