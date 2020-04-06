package io.branch.search;

import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

/**
 * SearchRequest Tests.
 */
@RunWith(AndroidJUnit4.class)
public class BranchSearchRequestTest {

    @Test
    public void testRequestCreation() throws Throwable {
        BranchSearchRequest requestIn = BranchSearchRequest.create("餐厅");

        requestIn.setMaxAppResults(100);
        requestIn.setMaxContentPerAppResults(200);
        requestIn.disableQueryModification();
        requestIn.setQuerySource(BranchQuerySource.QUERY_HINT_RESULTS);

        BranchDeviceInfo info = new BranchDeviceInfo();
        BranchConfiguration config = new BranchConfiguration();

        config.setBranchKey("key_live_123"); // need a "valid" key
        config.setCountryCode("ZZ");
        config.setGoogleAdID("XYZ");

        JSONObject jsonIn = BranchSearchInterface.createPayload(requestIn, config, info);
        Log.d("Branch", "SearchRequest::testRequestCreation(): " + jsonIn.toString());

        Assert.assertEquals("餐厅", jsonIn.getString(BranchSearchRequest.KEY_USER_QUERY));
        Assert.assertEquals(100, jsonIn.getInt(BranchSearchRequest.KEY_LIMIT_APP_RESULTS));
        Assert.assertEquals(200, jsonIn.getInt(BranchSearchRequest.KEY_LIMIT_LINK_RESULTS));
        Assert.assertTrue(jsonIn.getBoolean(BranchSearchRequest.KEY_DO_NOT_MODIFY));
        Assert.assertEquals(BranchQuerySource.QUERY_HINT_RESULTS.toString(),
                jsonIn.getString(BranchSearchRequest.KEY_QUERY_SOURCE));

        Assert.assertEquals("key_live_123", jsonIn.getString(BranchConfiguration.JSONKey.BranchKey.toString()));
        Assert.assertEquals("ZZ", jsonIn.getString(BranchConfiguration.JSONKey.Country.toString()));
        Assert.assertEquals("XYZ", jsonIn.getString(BranchConfiguration.JSONKey.GAID.toString()));
        Assert.assertFalse(TextUtils.isEmpty(jsonIn.getString(BranchConfiguration.JSONKey.Locale.toString())));

        Assert.assertEquals("ANDROID", jsonIn.getString(BranchDeviceInfo.JSONKey.OS.toString()));
    }

    @Test
    public void testHasDeviceInfo() throws Throwable {
        BranchSearchRequest request = BranchSearchRequest.create("MOD Pizza");
        JSONObject jsonOut = BranchSearchInterface.createPayload(request,
                new BranchConfiguration(), new BranchDeviceInfo());

        Assert.assertNotNull(jsonOut.getString(BranchDeviceInfo.JSONKey.Brand.toString()));
        Assert.assertNotNull(jsonOut.getString(BranchDeviceInfo.JSONKey.Model.toString()));
        Assert.assertNotNull(jsonOut.getString(BranchDeviceInfo.JSONKey.OSVersion.toString()));
        Assert.assertNotNull(jsonOut.getString(BranchDeviceInfo.JSONKey.Carrier.toString()));
    }

    @Test
    public void testHasQueryModificationFlag() throws Throwable {
        final String MODIFY_KEY = "do_not_modify";

        BranchSearchRequest request = BranchSearchRequest.create("MOD Pizza");
        JSONObject jsonOut = BranchSearchInterface.createPayload(request,
                new BranchConfiguration(), new BranchDeviceInfo());

        // Per Spec:  The field should not be sent if false.
        try {
            // In fact, we fully expect this to throw as the key should not exist.
            boolean flag = jsonOut.getBoolean(MODIFY_KEY);
            Assert.assertFalse(flag);
            Assert.fail();
        } catch(JSONException e) {
            Assert.assertFalse(jsonOut.optBoolean(MODIFY_KEY));
        }

        // "Disable" query modifications.  The key should now exist.
        request.disableQueryModification();
        jsonOut = BranchSearchInterface.createPayload(request,
                new BranchConfiguration(), new BranchDeviceInfo());
        Assert.assertTrue(jsonOut.getBoolean(MODIFY_KEY));
    }

    @Test
    public void testSetExtra() {
        BranchSearchRequest request = BranchSearchRequest.create("Pizza");
        BranchConfiguration configuration = new BranchConfiguration();
        BranchDeviceInfo info = new BranchDeviceInfo();
        JSONObject json;

        // If no extra is present, json should not even have the extra key.
        json = BranchSearchInterface.createPayload(request, configuration, info);
        Assert.assertFalse(json.has(BranchDiscoveryRequest.JSONKey.Extra.toString()));

        // If some extra is present, it will be inside a child json object.
        request.setExtra("theme", "dark");
        json = BranchSearchInterface.createPayload(request, configuration, info);
        JSONObject extra = json.optJSONObject(BranchDiscoveryRequest.JSONKey.Extra.toString());
        Assert.assertNotNull(extra);
        Assert.assertEquals("dark", extra.optString("theme"));

        // If null is passed, the object is cleared.
        request.setExtra("theme", null);
        json = BranchSearchInterface.createPayload(request, configuration, info);
        Assert.assertFalse(json.has(BranchDiscoveryRequest.JSONKey.Extra.toString()));
    }

    @Test
    public void testSetExtra_overridesConfiguration() {
        BranchDeviceInfo info = new BranchDeviceInfo();
        BranchConfiguration configuration = new BranchConfiguration();
        configuration.addRequestExtra("theme", "light");
        configuration.addRequestExtra("size", "small");

        BranchSearchRequest request = BranchSearchRequest.create("Pizza");
        request.setExtra("theme", "dark");
        JSONObject json = BranchSearchInterface.createPayload(request, configuration, info);
        JSONObject extra = json.optJSONObject(BranchDiscoveryRequest.JSONKey.Extra.toString());
        Assert.assertNotNull(extra);

        // Should override "theme" but leave "size" as is.
        Assert.assertEquals("dark", extra.optString("theme"));
        Assert.assertEquals("small", extra.optString("size"));
    }

    @Test
    public void testOverrideLocale() {
        String testLocale = "xx_YY";

        BranchSearchRequest request = BranchSearchRequest.create("MOD Pizza");
        BranchConfiguration config = new BranchConfiguration();
        BranchDeviceInfo info = new BranchDeviceInfo();

        JSONObject jsonObject = BranchSearchInterface.createPayload(request, config, info);

        String localeString = jsonObject.optString(BranchDeviceInfo.JSONKey.Locale.toString());
        Assert.assertFalse(TextUtils.isEmpty(localeString));
        Assert.assertNotSame(testLocale, localeString);

        config.setLocale(new Locale("xx_YY"));

        jsonObject = BranchSearchInterface.createPayload(request, config, info);
        Assert.assertEquals(testLocale.toLowerCase(), jsonObject.optString(BranchDeviceInfo.JSONKey.Locale.toString().toLowerCase()));
    }
}
