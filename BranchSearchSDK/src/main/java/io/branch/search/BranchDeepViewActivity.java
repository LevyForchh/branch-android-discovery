package io.branch.search;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * An activity that hosts a WebView where we can show deepviews.
 * See {@link BranchLinkResult#openDeepView(Context)}.
 */
public class BranchDeepViewActivity extends FragmentActivity {

    private static final String METADATA_KEY = "io.branch.sdk.DeepViewActivity";
    private static final String KEY_DESTINATION = "destination";

    @NonNull
    static Intent getIntent(@NonNull Context context, @NonNull BranchLinkResult result) {
        String ctaUrl = "https://play.google.com/store/apps/details?id="
                + result.getDestinationPackageName();
        String appUrl = result.getAppIconUrl().trim();
        String imageUrl = result.getImageUrl().trim();
        if (TextUtils.isEmpty(imageUrl)) {
            imageUrl = appUrl;
        }
        if (imageUrl.equals(appUrl)) {
            // Workaround for having bigger images in case the app url is being used
            // as the full width image url.
            imageUrl = appUrl.replace("=s90", "");
        }
        String key = BranchSearch.getInstance().getBranchConfiguration().getBranchKey();
        Uri destination = Uri.parse("https://search.app.link")
                .buildUpon()
                .appendPath("deepview-" + key)
                .appendQueryParameter("og_title", result.getName())
                .appendQueryParameter("og_description", result.getDescription())
                .appendQueryParameter("og_image_url", imageUrl)
                .appendQueryParameter("cta_url", ctaUrl)
                .appendQueryParameter("app_name", result.getAppName())
                .appendQueryParameter("app_image_url", appUrl)
                .build();
        return getIntent(context, destination);
    }

    @NonNull
    private static Intent getIntent(@NonNull Context context, @NonNull Uri destination) {
        // Find the target class name:
        // It defaults to this class but can be set to a subclass of it
        Class<?> target = BranchDeepViewActivity.class;
        try {
            PackageManager manager = context.getPackageManager();
            ApplicationInfo info = manager.getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            if (info.metaData != null && info.metaData.containsKey(METADATA_KEY)) {
                String targetName = info.metaData.getString(METADATA_KEY);
                if (!TextUtils.isEmpty(targetName)) {
                    //noinspection ConstantConditions
                    if (targetName.startsWith(".")) {
                        targetName = context.getPackageName() + targetName;
                    }
                    Class<?> candidate = Class.forName(targetName);
                    if (target.isAssignableFrom(candidate)) {
                        target = candidate;
                    } else {
                        throw new RuntimeException("A deepview activity was declared in Manifest," +
                                " but it does not extend BranchDeepViewActivity.");
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException | ClassNotFoundException ignore) {}

        // Check that the activity was correctly declared in the Manifest file,
        // something easy to forget so we want to print a clear message.
        boolean isDeclared = false;
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            for (ActivityInfo activity : info.activities) {
                if (target.getName().equals(activity.name)) {
                    isDeclared = true;
                    break;
                }
            }

        } catch (PackageManager.NameNotFoundException ignore) {}
        if (!isDeclared) {
            throw new RuntimeException("To open deepviews you must declare BranchDeepViewActivity" +
                    " or a subclass in your Manifest file. When using a subclass, you should also" +
                    " register it in the application metadata:\n\n" +
                    "<meta-data\n" +
                    "    android:name=\"io.branch.sdk.DeepViewActivity\"\n" +
                    "    android:value=\"com.package.CustomDeepViewActivity\"/>");
        }

        // We can go on.
        Intent intent = new Intent(context, target);
        intent.putExtra(KEY_DESTINATION, destination);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.branch_deepview_activity);
        setFinishOnTouchOutside(true);
        WebView webView = getWebView();
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(false);

        Uri destination = getIntent().getParcelableExtra(KEY_DESTINATION);
        if (destination != null) {
            webView.loadUrl(destination.toString());
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    // Special check for "close" button, in case one is present.
                    if (url.equals("http://close") || url.equals("http://close/")) {
                        finish();
                        return true;
                    } else if (url.contains("play.google.com")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        onPlayStoreClicked();
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        } else {
            throw new IllegalStateException("No destination!");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWebView().onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWebView().onResume();
    }

    /**
     * Returns the web view that is part of this activity hierarchy.
     * @return web view
     */
    @NonNull
    protected WebView getWebView() {
        return findViewById(R.id.branch_deepview_webview);
    }

    /**
     * Called when the call-to-action button to download the app has
     * been clicked. By default, this finishes the deep view activity.
     */
    protected void onPlayStoreClicked() {
        finish();
    }
}
