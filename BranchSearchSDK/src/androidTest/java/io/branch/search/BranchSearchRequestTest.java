package io.branch.search;

import android.support.annotation.NonNull;
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
 * {@link BranchSearchRequest} tests.
 */
@RunWith(AndroidJUnit4.class)
public class BranchSearchRequestTest extends BranchDiscoveryRequestTest {

    @NonNull
    @Override
    protected BranchDiscoveryRequest newRequest() {
        return BranchSearchRequest.create("pizza");
    }

    @Test
    public void testRequestFields() throws Throwable {
        BranchSearchRequest requestIn = BranchSearchRequest.create("餐厅");
        requestIn.setMaxAppResults(100);
        requestIn.setMaxContentPerAppResults(200);
        requestIn.disableQueryModification();
        requestIn.setQuerySource(BranchQuerySource.QUERY_HINT_RESULTS);

        BranchDeviceInfo info = new BranchDeviceInfo();
        BranchConfiguration config = new BranchConfiguration();
        JSONObject jsonIn = BranchSearchInterface.createPayload(requestIn, config, info);
        Log.d("Branch", "SearchRequest::testRequestCreation(): " + jsonIn.toString());

        Assert.assertEquals("餐厅", jsonIn.getString(BranchSearchRequest.KEY_USER_QUERY));
        Assert.assertEquals(100, jsonIn.getInt(BranchSearchRequest.KEY_LIMIT_APP_RESULTS));
        Assert.assertEquals(200, jsonIn.getInt(BranchSearchRequest.KEY_LIMIT_LINK_RESULTS));
        Assert.assertTrue(jsonIn.getBoolean(BranchSearchRequest.KEY_DO_NOT_MODIFY));
        Assert.assertEquals(BranchQuerySource.QUERY_HINT_RESULTS.toString(),
                jsonIn.getString(BranchSearchRequest.KEY_QUERY_SOURCE));
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
