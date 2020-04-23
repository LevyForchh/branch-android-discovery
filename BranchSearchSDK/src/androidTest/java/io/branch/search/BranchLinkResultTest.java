package io.branch.search;

import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.branch.search.util.AssetUtils;

/**
 * BranchLinkResult class tests.
 */
@RunWith(AndroidJUnit4.class)
public class BranchLinkResultTest extends BranchTest {

    @Test
    public void testParcelable_keepsEmptyStrings() throws JSONException  {
        // Create a BranchLinkResult from sample JSON
        JSONObject json = new JSONObject(AssetUtils.readJsonFile(getTestContext(), "link_example.json"));
        BranchLinkResult link1 = BranchLinkResult.createFromJson(json,
                "appName", "appStoreId", "appIconUrl", "");
        Assert.assertNotNull(link1);

        // Ensure missing fields are actually ""s, and the others were correctly set.
        Assert.assertEquals("", link1.getRankingHint());
        Assert.assertEquals("appName", link1.getAppName());
        Assert.assertEquals("appStoreId", link1.getDestinationPackageName());
        Assert.assertEquals("appIconUrl", link1.getAppIconUrl());

        // Parcel and unparcel.
        Parcel parcel = Parcel.obtain();
        link1.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        BranchLinkResult link2 = BranchLinkResult.CREATOR.createFromParcel(parcel);

        // Ensure read fields are what we'd expect.
        Assert.assertEquals("", link2.getRankingHint());
        Assert.assertEquals("appName", link2.getAppName());
        Assert.assertEquals("appStoreId", link2.getDestinationPackageName());
        Assert.assertEquals("appIconUrl", link2.getAppIconUrl());
    }
}
