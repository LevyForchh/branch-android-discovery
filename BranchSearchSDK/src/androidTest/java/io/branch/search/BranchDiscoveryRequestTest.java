package io.branch.search;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * {@link BranchDiscoveryRequest} tests.
 */
public abstract class BranchDiscoveryRequestTest {

    @NonNull
    protected abstract BranchDiscoveryRequest newRequest();

    @Test
    public void testTimestamp() {
        BranchDiscoveryRequest request = newRequest();
        BranchConfiguration configuration = new BranchConfiguration();
        BranchDeviceInfo info = new BranchDeviceInfo();
        JSONObject json = BranchSearchInterface.createPayload(request, configuration, info);
        Assert.assertTrue(json.has(BranchDiscoveryRequest.KEY_TIMESTAMP));
    }

    @Test
    public void testSetExtra() {
        BranchDiscoveryRequest request = newRequest();
        BranchConfiguration configuration = new BranchConfiguration();
        BranchDeviceInfo info = new BranchDeviceInfo();
        JSONObject json;

        // If no extra is present, json should not even have the extra key.
        json = BranchSearchInterface.createPayload(request, configuration, info);
        Assert.assertFalse(json.has(BranchDiscoveryRequest.KEY_EXTRA));

        // If some extra is present, it will be inside a child json object.
        request.setExtra("theme", "dark");
        json = BranchSearchInterface.createPayload(request, configuration, info);
        JSONObject extra = json.optJSONObject(BranchDiscoveryRequest.KEY_EXTRA);
        Assert.assertNotNull(extra);
        Assert.assertEquals("dark", extra.optString("theme"));

        // If null is passed, the object is cleared.
        request.setExtra("theme", null);
        json = BranchSearchInterface.createPayload(request, configuration, info);
        Assert.assertFalse(json.has(BranchDiscoveryRequest.KEY_EXTRA));
    }

    @Test
    public void testSetExtra_overridesConfiguration() {
        BranchDeviceInfo info = new BranchDeviceInfo();
        BranchConfiguration configuration = new BranchConfiguration();
        configuration.addRequestExtra("theme", "light");
        configuration.addRequestExtra("size", "small");

        BranchDiscoveryRequest request = newRequest();
        request.setExtra("theme", "dark");
        JSONObject json = BranchSearchInterface.createPayload(request, configuration, info);
        JSONObject extra = json.optJSONObject(BranchDiscoveryRequest.KEY_EXTRA);
        Assert.assertNotNull(extra);

        // Should override "theme" but leave "size" as is.
        Assert.assertEquals("dark", extra.optString("theme"));
        Assert.assertEquals("small", extra.optString("size"));
    }

    // NOTE: This is not testing the request classes... Move to BranchSearchInterfaceTest?
    @Test
    public void testHasDeviceInfo() throws Throwable {
        BranchDiscoveryRequest request = newRequest();
        JSONObject jsonOut = BranchSearchInterface.createPayload(request,
                new BranchConfiguration(), new BranchDeviceInfo());

        Assert.assertNotNull(jsonOut.getString(BranchDeviceInfo.JSONKey.Brand.toString()));
        Assert.assertNotNull(jsonOut.getString(BranchDeviceInfo.JSONKey.Model.toString()));
        Assert.assertNotNull(jsonOut.getString(BranchDeviceInfo.JSONKey.OSVersion.toString()));
        Assert.assertNotNull(jsonOut.getString(BranchDeviceInfo.JSONKey.Carrier.toString()));
        Assert.assertEquals("ANDROID", jsonOut.getString(BranchDeviceInfo.JSONKey.OS.toString()));
    }

    // NOTE: This is not testing the request classes... Move to BranchSearchInterfaceTest?
    @Test
    public void testHasConfiguration() throws Throwable {
        BranchDiscoveryRequest request = newRequest();
        BranchDeviceInfo info = new BranchDeviceInfo();
        BranchConfiguration config = new BranchConfiguration();
        config.setBranchKey("key_live_123"); // need a "valid" key
        config.setCountryCode("ZZ");
        config.setGoogleAdID("XYZ");
        JSONObject json = BranchSearchInterface.createPayload(request, config, info);
        Assert.assertEquals("key_live_123", json.getString(BranchConfiguration.JSONKey.BranchKey.toString()));
        Assert.assertEquals("ZZ", json.getString(BranchConfiguration.JSONKey.Country.toString()));
        Assert.assertEquals("XYZ", json.getString(BranchConfiguration.JSONKey.GAID.toString()));
        Assert.assertFalse(TextUtils.isEmpty(json.getString(BranchConfiguration.JSONKey.Locale.toString())));
    }
}
