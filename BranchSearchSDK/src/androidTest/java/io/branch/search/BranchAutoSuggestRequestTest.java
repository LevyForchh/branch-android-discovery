package io.branch.search;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * {@link BranchAutoSuggestRequest} tests.
 */
@RunWith(AndroidJUnit4.class)
public class BranchAutoSuggestRequestTest extends BranchDiscoveryRequestTest {

    @NonNull
    @Override
    protected BranchDiscoveryRequest newRequest() {
        return BranchAutoSuggestRequest.create("pizza");
    }

    @Test
    public void testQuery() throws Throwable {
        BranchAutoSuggestRequest request = BranchAutoSuggestRequest.create("foo");
        BranchConfiguration config = new BranchConfiguration();
        BranchDeviceInfo info = new BranchDeviceInfo();
        JSONObject json = BranchSearchInterface.createPayload(request, config, info);
        Assert.assertEquals("foo", json.getString(BranchAutoSuggestRequest.KEY_USER_QUERY));
    }

    @Test
    public void testMaxResults() throws Throwable {
        BranchAutoSuggestRequest requestIn = BranchAutoSuggestRequest.create("foo");
        BranchConfiguration config = new BranchConfiguration();
        BranchDeviceInfo info = new BranchDeviceInfo();

        requestIn.setMaxResults(12);
        JSONObject json = BranchSearchInterface.createPayload(requestIn, config, info);
        Assert.assertEquals(12, json.getInt(BranchAutoSuggestRequest.KEY_MAX_RESULTS));

        requestIn.setMaxResults(-1);
        json = BranchSearchInterface.createPayload(requestIn, config, info);
        Assert.assertFalse(json.has(BranchAutoSuggestRequest.KEY_MAX_RESULTS));
    }
}
