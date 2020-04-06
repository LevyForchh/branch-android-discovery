package io.branch.search;

import android.support.test.runner.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.branch.search.util.AssetUtils;

/**
 * {@link BranchAutoSuggestResult} Test.
 */
@RunWith(AndroidJUnit4.class)
public class BranchAutoSuggestResultTest extends BranchTest {

    @Test
    public void testResultSuccess() throws Throwable {
        String response = AssetUtils.readJsonFile(getTestContext(), "success_autosuggest.json");
        Assert.assertTrue(response.length() > 0);

        JSONObject jsonResponse = new JSONObject(response);

        BranchAutoSuggestResult result = BranchAutoSuggestResult.createFromJson(jsonResponse);
        Assert.assertNotNull(result);

        Assert.assertEquals(3, result.getSuggestions().size());
    }

    @Test
    public void testResultSuccess_empty() throws Throwable {
        String response = AssetUtils.readJsonFile(getTestContext(), "success_autosuggest_empty.json");
        Assert.assertTrue(response.length() > 0);

        JSONObject jsonResponse = new JSONObject(response);

        BranchAutoSuggestResult result = BranchAutoSuggestResult.createFromJson(jsonResponse);
        Assert.assertNotNull(result);

        Assert.assertEquals(0, result.getSuggestions().size());
    }

    @Test
    public void testResultError() throws Throwable {
        String response = AssetUtils.readJsonFile(getTestContext(), "err_autosuggest.json");
        Assert.assertTrue(response.length() > 0);

        JSONObject jsonObject = new JSONObject(response);

        BranchSearchError error = new BranchSearchError(jsonObject);
        Assert.assertEquals(error.getErrorCode(), BranchSearchError.ERR_CODE.BAD_REQUEST_ERR);
    }
}
