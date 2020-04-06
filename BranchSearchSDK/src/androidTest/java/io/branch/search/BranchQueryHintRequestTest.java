package io.branch.search;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * {@link BranchQueryHintRequest} tests.
 */
@RunWith(AndroidJUnit4.class)
public class BranchQueryHintRequestTest extends BranchDiscoveryRequestTest {

    @NonNull
    @Override
    protected BranchDiscoveryRequest newRequest() {
        return BranchQueryHintRequest.create();
    }

    @Test
    public void testMaxResults() throws Throwable {
        BranchQueryHintRequest requestIn = BranchQueryHintRequest.create();
        BranchConfiguration config = new BranchConfiguration();
        BranchDeviceInfo info = new BranchDeviceInfo();

        requestIn.setMaxResults(12);
        JSONObject json = BranchSearchInterface.createPayload(requestIn, config, info);
        Assert.assertEquals(12, json.getInt(BranchQueryHintRequest.KEY_MAX_RESULTS));

        requestIn.setMaxResults(-1);
        json = BranchSearchInterface.createPayload(requestIn, config, info);
        Assert.assertFalse(json.has(BranchQueryHintRequest.KEY_MAX_RESULTS));
    }
}
