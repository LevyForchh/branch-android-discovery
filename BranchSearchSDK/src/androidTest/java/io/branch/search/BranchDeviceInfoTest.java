package io.branch.search;

import android.content.Intent;
import android.os.Build;
import android.support.test.runner.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * {@link BranchDeviceInfo} class tests.
 */
@RunWith(AndroidJUnit4.class)
public class BranchDeviceInfoTest extends BranchTest {

    @Test
    public void testDefaults() {
        BranchDeviceInfo info = new BranchDeviceInfo();

        // Statically initialized fields
        Assert.assertEquals(Build.MANUFACTURER, info.getBrand());
        Assert.assertEquals(Build.MODEL, info.getModel());
        Assert.assertEquals(Build.VERSION.SDK_INT, info.getOSVersion());

        // Before initialization, fields should be null/default
        Assert.assertEquals(BranchDeviceInfo.DEFAULT_LOCALE, info.getLocale());
        Assert.assertEquals(BranchDeviceInfo.UNKNOWN_CARRIER, info.getCarrier());
        Assert.assertNull(info.getAppPackage());
        Assert.assertNull(info.getAppVersion());

        info.sync(getTestContext());

        // After initialization, fields should be set
        // Can't test locale/carrier as they depend on the device running the test suite
        Assert.assertNotNull(info.getAppPackage());
        Assert.assertNotNull(info.getAppVersion());
    }

    @Test
    public void testAddDeviceInfo() {
        BranchDeviceInfo info = new BranchDeviceInfo();
        info.sync(getTestContext());
        info.latitude = 50;
        info.longitude = 30;
        JSONObject object = new JSONObject();
        info.addDeviceInfo(object);

        Assert.assertEquals(50, object.optDouble(BranchDeviceInfo.JSONKey.Latitude.toString()), 0.01);
        Assert.assertEquals(30, object.optDouble(BranchDeviceInfo.JSONKey.Longitude.toString()), 0.01);
        Assert.assertTrue(object.has(BranchDeviceInfo.JSONKey.AppPackage.toString()));
        Assert.assertTrue(object.has(BranchDeviceInfo.JSONKey.AppVersion.toString()));
    }
}
