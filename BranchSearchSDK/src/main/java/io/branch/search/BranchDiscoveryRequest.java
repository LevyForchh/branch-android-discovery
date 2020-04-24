package io.branch.search;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Request model for Branch Discovery.
 */
@SuppressWarnings("unchecked")
public class BranchDiscoveryRequest<T extends BranchDiscoveryRequest> {

    final static String KEY_REQUEST_ID = "request_id";
    final static String KEY_TIMESTAMP = "utc_timestamp";
    // We want to override the configuration-level extras
    final static String KEY_EXTRA = BranchConfiguration.JSONKey.RequestExtra.toString();

    private final Map<String, Object> extra = new HashMap<>();

    /**
     * Private Constructor.
     */
    BranchDiscoveryRequest() {
    }

    /**
     * Set the current location.
     * @param location Location
     * @return this BranchDiscoveryRequest
     * @deprecated please use {@link BranchSearch#setLocation(double, double)} instead
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public T setLocation(Location location) {
        if (location != null) {
            setLatitude(location.getLatitude());
            setLongitude(location.getLongitude());
        }
        return (T) this;
    }

    /**
     * Set the current location - latitude.
     * @param latitude latitude
     * @return this BranchDiscoveryRequest
     * @deprecated please use {@link BranchSearch#setLocation(double, double)} instead
     */
    @Deprecated
    @SuppressWarnings("UnusedReturnValue")
    public T setLatitude(double latitude) {
        BranchSearch search = BranchSearch.getInstance();
        if (search != null) search.getBranchDeviceInfo().latitude = latitude;
        return (T) this;
    }

    /**
     * Set the current location - longitude.
     * @param longitude latitude
     * @return this BranchDiscoveryRequest
     * @deprecated please use {@link BranchSearch#setLocation(double, double)} instead
     */
    @Deprecated
    @SuppressWarnings("UnusedReturnValue")
    public T setLongitude(double longitude) {
        BranchSearch search = BranchSearch.getInstance();
        if (search != null) search.getBranchDeviceInfo().longitude = longitude;
        return (T) this;
    }

    /**
     * Adds extra data to be passed to server in form
     * of a key-value pair. This value will override any other value for the
     * same key that was previously set and values that were set at the configuration
     * level using {@link BranchConfiguration#addRequestExtra(String, Object)}.
     *
     * Passing null as a value will clear any extra that was previously set in this request
     * (but not those set at the configuration level).
     *
     * @param key a key
     * @param data value
     * @return this BranchDiscoveryRequest
     */
    @SuppressWarnings("WeakerAccess")
    @NonNull
    public T setExtra(@NonNull String key, @Nullable Object data) {
        if (data == null) {
            extra.remove(key);
        } else {
            extra.put(key, data);
        }
        return (T) this;
    }

    @NonNull
    JSONObject toJson() {
        JSONObject object = new JSONObject();
        try {
            // Add the current timestamp.
            object.putOpt(KEY_TIMESTAMP, System.currentTimeMillis());

            // Add extra data.
            // The JSONObject for this key is shared between this class and BranchConfiguration.
            // But our extra data has priority so BranchConfiguration will never overwrite these values.
            if (!extra.keySet().isEmpty()) {
                JSONObject extraData = new JSONObject();
                for (String key : extra.keySet()) {
                    Object value = extra.get(key);
                    extraData.putOpt(key, value);
                }
                object.putOpt(KEY_EXTRA, extraData);
            }
        } catch (JSONException ignore) { }
        return object;
    }
}
