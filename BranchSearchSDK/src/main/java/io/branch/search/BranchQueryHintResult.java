package io.branch.search;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents results of a query hint query started from
 * {@link BranchSearch#queryHint(BranchQueryHintRequest, IBranchQueryHintEvents)}.
 */
public class BranchQueryHintResult implements Parcelable {
    private static final String KEY_RESULTS = "results";

    private final List<BranchQueryHint> hints;

    private BranchQueryHintResult(@NonNull List<BranchQueryHint> hints) {
        this.hints = hints;
    }

    @NonNull
    public List<BranchQueryHint> getHints() {
        return hints;
    }

    @NonNull
    static BranchQueryHintResult createFromJson(@NonNull JSONObject jsonObject) {
        List<BranchQueryHint> hints = new ArrayList<>();
        try {
            JSONArray jsonArray = jsonObject.optJSONArray(KEY_RESULTS);
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    hints.add(new BranchQueryHint(jsonArray.getString(i)));
                }
            }
        } catch (JSONException ignore) { }
        return new BranchQueryHintResult(hints);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(hints);
    }

    public final static Creator<BranchQueryHintResult> CREATOR = new Creator<BranchQueryHintResult>() {
        @Override
        public BranchQueryHintResult createFromParcel(Parcel source) {
            List<BranchQueryHint> hints = new ArrayList<>();
            source.readTypedList(hints, BranchQueryHint.CREATOR);
            return new BranchQueryHintResult(hints);
        }

        @Override
        public BranchQueryHintResult[] newArray(int size) {
            return new BranchQueryHintResult[size];
        }
    };
}
