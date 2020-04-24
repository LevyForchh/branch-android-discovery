package io.branch.search;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.intent.matcher.IntentMatchers;
import android.support.test.runner.AndroidJUnit4;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import io.branch.search.util.AssetUtils;

/**
 * {@link BranchLinkHandler} tests.
 */
@RunWith(AndroidJUnit4.class)
public class BranchLinkHandlerTest extends BranchTest {

    private BranchLinkResult link;
    private IBranchShortcutHandler externalShortutHandler;
    private IBranchDeepViewHandler externalDeepViewHandler;

    @Before
    public void setUp() throws Throwable {
        super.setUp();
        externalShortutHandler = Mockito.mock(IBranchShortcutHandler.class);
        externalDeepViewHandler = Mockito.mock(IBranchDeepViewHandler.class);
        BranchConfiguration configuration = new BranchConfiguration();
        configuration.setShortcutHandler(externalShortutHandler);
        configuration.setDeepViewHandler(externalDeepViewHandler);
        initBranch(configuration);

        JSONObject linkJson = load("link_example");
        link = BranchLinkResult.createFromJson(linkJson, "Sample App",
                "com.sample", "", "", "", "");
        Assert.assertNotNull(link);

        Intents.init();
    }

    @After
    public void tearDown() {
        super.tearDown();
        Intents.release();
    }

    @NonNull
    private JSONObject load(@NonNull String name) throws JSONException  {
        return new JSONObject(AssetUtils.readJsonFile(getTestContext(), name + ".json"));
    }

    @Test(expected = JSONException.class)
    public void testShortcut_malformed() throws JSONException {
        JSONObject json = load("handler_shortcut_malformed");
        BranchLinkHandler.from(json);
    }

    @Test
    public void testShortcut_validate() throws JSONException {
        JSONObject json = load("handler_shortcut");
        BranchLinkHandler handler = BranchLinkHandler.from(json);
        handler.validate(getTestContext(), link);
        Mockito.verify(externalShortutHandler, Mockito.times(1))
                .validateShortcut(Mockito.eq(getTestContext()), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testShortcut_open() throws JSONException {
        JSONObject json = load("handler_shortcut");
        BranchLinkHandler handler = BranchLinkHandler.from(json);
        handler.open(getTestContext(), link);
        Mockito.verify(externalShortutHandler, Mockito.times(1))
                .launchShortcut(Mockito.eq(getTestContext()), Mockito.anyString(), Mockito.anyString());
    }

    @Test(expected = JSONException.class)
    public void testViewIntent_malformed() throws JSONException {
        JSONObject json = load("handler_view_intent_malformed");
        BranchLinkHandler.from(json);
    }

    @Test
    public void testViewIntent_validate() throws JSONException {
        JSONObject json1 = load("handler_view_intent_1");
        JSONObject json2 = load("handler_view_intent_2");
        BranchLinkHandler handler1 = BranchLinkHandler.from(json1);
        BranchLinkHandler handler2 = BranchLinkHandler.from(json2);
        Assert.assertTrue(handler1.validate(getTestContext(), link));
        Assert.assertFalse(handler2.validate(getTestContext(), link));
    }

    @Test
    public void testViewIntent_open() throws JSONException {
        JSONObject json = load("handler_view_intent_1");
        BranchLinkHandler handler = BranchLinkHandler.from(json);
        Assert.assertTrue(handler.open(getTestContext(), link));
        Intents.intended(IntentMatchers.hasAction(Intent.ACTION_VIEW));
        Intents.intended(IntentMatchers.hasExtra("foo1", "bar1"));
        Intents.intended(IntentMatchers.hasExtra("foo2", "bar2"));
    }

    @Test
    public void testLaunchIntent_validate() throws JSONException {
        // launch_intent takes the target package name from the link.
        // We made it up, so validation should fail.
        JSONObject json = load("handler_launch_intent");
        BranchLinkHandler handler = BranchLinkHandler.from(json);
        Assert.assertFalse(handler.validate(getTestContext(), link));
    }

    @Test
    public void testLaunchIntent_open() throws JSONException {
        // launch_intent takes the target package name from the link.
        // We made it up, so opening should fail. It won't be able to find the launch intent.
        JSONObject json = load("handler_launch_intent");
        BranchLinkHandler handler = BranchLinkHandler.from(json);
        Assert.assertFalse(handler.open(getTestContext(), link));
        Intents.assertNoUnverifiedIntents();
    }

    @Test(expected = JSONException.class)
    public void testCustomIntent_malformed() throws JSONException {
        JSONObject json = load("handler_custom_intent_malformed");
        BranchLinkHandler.from(json);
    }

    @Test
    public void testCustomIntent_validate() throws JSONException {
        JSONObject json = load("handler_custom_intent");
        BranchLinkHandler handler = BranchLinkHandler.from(json);
        Assert.assertTrue(handler.validate(getTestContext(), link));
    }

    @Test
    public void testCustomIntent_open() throws JSONException {
        JSONObject json = load("handler_custom_intent");
        BranchLinkHandler handler = BranchLinkHandler.from(json);
        Assert.assertTrue(handler.open(getTestContext(), link));
        Intents.intended(IntentMatchers.hasAction("android.settings.SETTINGS"));
        Intents.intended(IntentMatchers.hasExtra("foo1", "bar1"));
        Intents.intended(IntentMatchers.hasExtra("foo2", "bar2"));
    }

    @Test(expected = JSONException.class)
    public void testTestInstalled_malformed() throws JSONException {
        JSONObject json = load("handler_test_installed_malformed");
        BranchLinkHandler.from(json);
    }

    @Test
    public void testTestInstalled_validate() throws JSONException {
        JSONObject json1 = load("handler_test_installed_1");
        JSONObject json2 = load("handler_test_installed_2");
        BranchLinkHandler handler1 = BranchLinkHandler.from(json1);
        BranchLinkHandler handler2 = BranchLinkHandler.from(json2);
        Assert.assertTrue(handler1.validate(getTestContext(), link));
        Assert.assertFalse(handler2.validate(getTestContext(), link));
    }

    @Test
    public void testTestInstalled_open() throws JSONException {
        JSONObject json = load("handler_test_installed_1");
        BranchLinkHandler handler = BranchLinkHandler.from(json);
        Assert.assertTrue(handler.open(getTestContext(), link));
        Intents.intended(IntentMatchers.hasAction(Intent.ACTION_VIEW));
        Intents.intended(IntentMatchers.hasData("https://google.com"));
    }

    @Test(expected = JSONException.class)
    public void testTestNotInstalled_malformed() throws JSONException {
        JSONObject json = load("handler_test_not_installed_malformed");
        BranchLinkHandler.from(json);
    }

    @Test
    public void testTestNotInstalled_validate() throws JSONException {
        JSONObject json1 = load("handler_test_not_installed_1");
        JSONObject json2 = load("handler_test_not_installed_2");
        BranchLinkHandler handler1 = BranchLinkHandler.from(json1);
        BranchLinkHandler handler2 = BranchLinkHandler.from(json2);
        Assert.assertTrue(handler1.validate(getTestContext(), link));
        Assert.assertFalse(handler2.validate(getTestContext(), link));
    }

    @Test
    public void testTestNotInstalled_open() throws JSONException {
        JSONObject json = load("handler_test_not_installed_1");
        BranchLinkHandler handler = BranchLinkHandler.from(json);
        Assert.assertTrue(handler.open(getTestContext(), link));
        Intents.intended(IntentMatchers.hasAction(Intent.ACTION_VIEW));
        Intents.intended(IntentMatchers.hasData("https://google.com"));
    }

    @Test
    public void testDeepView_validate() throws JSONException {
        // deep_view handler is valid if child handlers are valid.
        JSONObject json1 = load("handler_deep_view_1");
        JSONObject json2 = load("handler_deep_view_2");
        BranchLinkHandler handler1 = BranchLinkHandler.from(json1);
        BranchLinkHandler handler2 = BranchLinkHandler.from(json2);
        Assert.assertTrue(handler1.validate(getTestContext(), link));
        Assert.assertFalse(handler2.validate(getTestContext(), link));
    }

    @Test
    public void testDeepView_open() throws JSONException {
        JSONObject json = load("handler_deep_view_1");
        BranchLinkHandler handler = BranchLinkHandler.from(json);
        Mockito.when(externalDeepViewHandler.launchDeepView(
                Mockito.eq(getTestContext()),
                Mockito.any(BranchDeepViewFragment.class))
        ).thenReturn(true);
        Assert.assertTrue(handler.open(getTestContext(), link));
        Mockito.verify(externalDeepViewHandler, Mockito.times(1)).launchDeepView(
                Mockito.eq(getTestContext()),
                Mockito.any(BranchDeepViewFragment.class));
    }
}
