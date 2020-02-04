package io.branch.search;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Class for representing a a deep link to content
 */
public class BranchLinkResult implements Parcelable {
    private static final String LINK_ENTITY_ID_KEY = "entity_id";
    private static final String LINK_TYPE_KEY = "type";
    private static final String LINK_SCORE_KEY = "score";
    private static final String LINK_NAME_KEY = "name";
    private static final String LINK_DESC_KEY = "description";
    private static final String LINK_IMAGE_URL_KEY = "image_url";
    private static final String LINK_METADATA_KEY = "metadata";
    private static final String LINK_URI_SCHEME_KEY = "uri_scheme";
    private static final String LINK_WEB_LINK_KEY = "web_link";
    private static final String LINK_ROUTING_MODE_KEY = "routing_mode";
    private static final String LINK_TRACKING_KEY = "click_tracking_link";
    private static final String LINK_RANKING_HINT_KEY = "ranking_hint";
    private static final String LINK_ANDROID_INTENT_KEY = "android_intent";

    private static final String ANDROID_INTENT_FIELDS_KEY = "fields";
    private static final String ANDROID_INTENT_EXTRAS_KEY = "extras";

    private static final String ANDROID_INTENT_FIELDS_ACTION_KEY = "intent:android:action";
    private static final String ANDROID_INTENT_FIELDS_PACKAGE_KEY = "intent:android:targetPackage";
    private static final String ANDROID_INTENT_FIELDS_CLASS_KEY = "intent:android:targetClass";
    private static final String ANDROID_INTENT_FIELDS_DATA_KEY = "intent:android:data";

    private String entity_id;
    private String name;
    private String description;
    private String image_url;
    private String app_name;
    private String app_icon_url;
    private String ranking_hint;
    private JSONObject metadata;
    private String type;
    private float score;

    private String routing_mode;
    private String uri_scheme;
    private String web_link;
    private String destination_store_id;
    private String click_tracking_url;
    private Intent android_intent;

    private BranchLinkResult() {
    }

    public String getEntityID() {
        return entity_id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return image_url;
    }

    public String getAppName() {
        return app_name;
    }

    public String getAppIconUrl() {
        return app_icon_url;
    }

    public String getType() {
        return type;
    }

    public float getScore() {
        return score;
    }

    public JSONObject getMetadata() {
        return metadata;
    }

    public String getRankingHint() {
        return ranking_hint;
    }

    public String getRoutingMode() {
        return routing_mode;
    }

    public String getUriScheme() {
        return uri_scheme;
    }

    public String getWebLink() {
        String webLink = web_link;
        if (webLink == null) {
            webLink = "https://play.google.com/store/apps/details?id=" + destination_store_id;
        }
        return webLink;
    }

    public String getDestinationPackageName() {
        return destination_store_id;
    }

    public String getClickTrackingUrl() {
        return click_tracking_url;
    }

    /**
     * This method allows you to register a click event on the action, which informs Branch
     * which item was clicked, improving the rankings and personalization over time
     */
    public void registerClickEvent() {
        if (!TextUtils.isEmpty(click_tracking_url)) {
            // Fire off an async click event
            BranchSearch.getInstance()
                    .getNetworkHandler(BranchSearch.Channel.SEARCH)
                    .executeGet(click_tracking_url, null);
        }
    }

    /**
     * Opens the link into a <a href="https://branch.io/deepviews/">Branch Deepview</a>.
     * The content preview will be rendered inside a native view with the option to
     * download the app from the play store.
     *
     * @param manager a fragment manager
     * @return an error if the deep view could not be opened
     */
    @SuppressWarnings("unused")
    @Nullable
    public BranchSearchError openDeepView(@NonNull FragmentManager manager) {
        registerClickEvent();

        // NOTE: We never return an error, but we might in a future implementation.
        // This also is consistent with openContent(Context, boolean).
        BranchDeepViewFragment fragment = BranchDeepViewFragment.getInstance(this);
        fragment.show(manager, BranchDeepViewFragment.TAG);
        return null;
    }

    /**
     * Completes the action associated with this object.
     * 1. Try to open the app with deep linking, if the app is installed on the device
     * 2. Opens the browser with the web fallback link
     * 3. Opens the play store if none of the above succeeded and fallbackToPlayStore is set to true
     * @param context             Application context
     * @param fallbackToPlayStore If set to {@code true} fallbacks to the Google play if the app is not installed and there is no valid web url.
     * @return BranchSearchError {@link BranchSearchError} object to pass any error with complete action. Null if succeeded.
     */
    public BranchSearchError openContent(Context context, boolean fallbackToPlayStore) {
        registerClickEvent();

        // 1. Try to open the app with an Android Intent
        boolean success = openAppWithAndroidIntent(context);

        // 2. Try to open the app directly with URI Scheme
        if (!success) {
            success = openAppWithUriScheme(context);
        }

        // 3. If URI Scheme is not working try opening with the web link in browser
        if (!success) {
            success = openAppWithWebLink(context);
        }

        // 4. Fallback to the playstore
        if (!success && fallbackToPlayStore) {
            success = openAppWithPlayStore(context);
        }

        BranchSearchError err = null;
        if (!success) {
            err = new BranchSearchError(BranchSearchError.ERR_CODE.ROUTING_ERR_UNABLE_TO_OPEN_APP);
        }
        return err;
    }

    private boolean openAppWithAndroidIntent(@NonNull Context context) {
        if (android_intent == null) return false;
        ResolveInfo info = context.getPackageManager().resolveActivity(android_intent, 0);
        if (info == null) {
            // Something in this android_intent is wrong. It might be the package name, the
            // target class name (if present), the intent action. There's nothing we can do.
            return false;
        }
        if (android_intent.getComponent() == null) {
            // No target class in server payload. Since we have already resolved one, let's add it.
            android_intent.setClassName(
                    info.activityInfo.packageName,
                    info.activityInfo.name);
        }
        context.startActivity(android_intent);
        return true;
    }

    private boolean openAppWithUriScheme(Context context) {
        boolean isAppOpened = false;
        int intentFlags = BranchSearch.getInstance().getBranchConfiguration().getLaunchIntentFlags();

        try {
            if (!TextUtils.isEmpty(uri_scheme)) {
                Uri uri = Uri.parse(uri_scheme);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setFlags(intentFlags);
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);
                    isAppOpened = true;
                }
            }
        } catch (Exception ignore) {
            // Nothing to do
        }
        return isAppOpened;
    }

    private boolean openAppWithWebLink(Context context) {
        boolean isAppOpened = false;

        try {
            if (!TextUtils.isEmpty(getWebLink())) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getWebLink()));
                context.startActivity(intent);

                isAppOpened = true;
            }
        } catch (Exception ignore) {
            // Nothing to do
        }
        return isAppOpened;
    }

    private boolean openAppWithPlayStore(Context context) {
        boolean isAppOpened = false;

        try {
            if (!TextUtils.isEmpty(destination_store_id)) {
                isAppOpened = Util.openAppInPlayStore(context, destination_store_id);
            }
        } catch (Exception ignore) {
            // Nothing to do
        }
        return isAppOpened;
    }

    @NonNull
    static BranchLinkResult createFromJson(@NonNull JSONObject actionJson,
                                           @NonNull String appName,
                                           @NonNull String appStoreId,
                                           @NonNull String appIconUrl) {
        BranchLinkResult link = new BranchLinkResult();
        link.entity_id = Util.optString(actionJson, LINK_ENTITY_ID_KEY);
        link.type = Util.optString(actionJson, LINK_TYPE_KEY);
        link.score = (float)actionJson.optDouble(LINK_SCORE_KEY);

        link.name = Util.optString(actionJson, LINK_NAME_KEY);
        link.description = Util.optString(actionJson, LINK_DESC_KEY);
        link.image_url = Util.optString(actionJson, LINK_IMAGE_URL_KEY);
        link.app_name = appName;
        link.app_icon_url = appIconUrl;
        link.ranking_hint = Util.optString(actionJson, LINK_RANKING_HINT_KEY);
        link.metadata = actionJson.optJSONObject(LINK_METADATA_KEY);

        link.routing_mode = Util.optString(actionJson, LINK_ROUTING_MODE_KEY);
        link.uri_scheme = Util.optString(actionJson, LINK_URI_SCHEME_KEY);
        link.web_link = Util.optString(actionJson, LINK_WEB_LINK_KEY);
        link.destination_store_id = appStoreId;

        link.click_tracking_url = Util.optString(actionJson, LINK_TRACKING_KEY);

        JSONObject androidIntent = actionJson.optJSONObject(LINK_ANDROID_INTENT_KEY);
        if (androidIntent != null) {
            JSONObject fields = androidIntent.optJSONObject(ANDROID_INTENT_FIELDS_KEY);
            if (fields == null) throw new RuntimeException("Unexpected android_intent data (no fields)");

            String action = Util.optString(fields, ANDROID_INTENT_FIELDS_ACTION_KEY);
            link.android_intent = new Intent();
            link.android_intent.setAction(action);
            link.android_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            String data = Util.optString(fields, ANDROID_INTENT_FIELDS_DATA_KEY);
            if (!TextUtils.isEmpty(data)) {
                link.android_intent.setData(Uri.parse(data));
            }

            String packageName = Util.optString(fields, ANDROID_INTENT_FIELDS_PACKAGE_KEY);
            if (TextUtils.isEmpty(packageName)) packageName = appStoreId;
            String className = Util.optString(fields, ANDROID_INTENT_FIELDS_CLASS_KEY);
            if (!TextUtils.isEmpty(className)) {
                link.android_intent.setClassName(packageName, className);

            } else {
                link.android_intent.setPackage(packageName);
            }

            JSONObject extras = androidIntent.optJSONObject(ANDROID_INTENT_EXTRAS_KEY);
            if (extras != null) {
                while (extras.keys().hasNext()) {
                    String key = extras.keys().next();
                    String value = Util.optString(extras, key);
                    link.android_intent.putExtra(key, value);
                }
            }

            // TODO (1) "shortcut:android:enabled"     - is this important?
            // TODO (2) "shortcut:android:icon"        - should we try to read this?
            //  It means we should access target app resources, then create a content:// uri provider,
            //  then provide the local uri in getImageUrl()... Not sure if possible. Would also be a breaking
            //  change for people that expect getImageUrl() to be an http(s):// uri.
            //  To read it we'd also need a Context - see (4).
            // TODO (3) "shortcut:android:label"       - will this be added?
            //  If so, we should probably read the string and replace the name of the BranchLinkResult.
            //  To read it we'd also need a Context - see (4).
            // TODO (4) do we need to filter out results that are not working here at parsing time,
            //  instead of click time? We'd need a PackageManager here, which means needing a Context,
            //  which means saving a static reference of it in BranchConfiguration. A breaking change
            //  for users and an important design change for the SDK.
        }

        return link;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.entity_id);
        dest.writeString(this.type);
        dest.writeFloat(this.score);

        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeString(this.image_url);
        dest.writeString(this.app_name);
        dest.writeString(this.app_icon_url);
        dest.writeString(this.ranking_hint);
        dest.writeString(this.metadata.toString());

        dest.writeString(this.routing_mode);
        dest.writeString(this.uri_scheme);
        dest.writeString(this.web_link);
        dest.writeString(this.destination_store_id);
        dest.writeString(this.click_tracking_url);
        dest.writeParcelable(this.android_intent, 0);
    }


    private BranchLinkResult(@NonNull Parcel in, @NonNull ClassLoader loader) {
        this.entity_id = in.readString();
        this.type = in.readString();
        this.score = in.readFloat();

        this.name = in.readString();
        this.description = in.readString();
        this.image_url = in.readString();
        this.app_name = in.readString();
        this.app_icon_url = in.readString();
        try {
            this.metadata = new JSONObject(in.readString());
        } catch (JSONException e) {
            this.metadata = new JSONObject();
        }
        this.ranking_hint = in.readString();

        this.routing_mode = in.readString();
        this.uri_scheme = in.readString();
        this.web_link = in.readString();
        this.destination_store_id = in.readString();
        this.click_tracking_url = in.readString();
        this.android_intent = in.readParcelable(loader);
    }

    public static final Creator<BranchLinkResult> CREATOR = new ClassLoaderCreator<BranchLinkResult>() {
        @Override
        public BranchLinkResult createFromParcel(Parcel source) {
            return createFromParcel(source, getClass().getClassLoader());
        }

        @Override
        public BranchLinkResult createFromParcel(Parcel source, ClassLoader loader) {
            return new BranchLinkResult(source, loader);
        }

        @Override
        public BranchLinkResult[] newArray(int size) {
            return new BranchLinkResult[size];
        }
    };

}
