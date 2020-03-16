package io.branch.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Class for representing a a deep link to content
 */
public class BranchLinkResult implements Parcelable {
    public static final String ICON_CATEGORY_BUSINESS = "business";
    public static final String ICON_CATEGORY_CARS = "cars";
    public static final String ICON_CATEGORY_COMMUNICATION = "communication";
    public static final String ICON_CATEGORY_EDUCATION = "education";
    public static final String ICON_CATEGORY_EVENTS = "events";
    public static final String ICON_CATEGORY_FOOD = "food";
    public static final String ICON_CATEGORY_GAMES = "games";
    public static final String ICON_CATEGORY_HEALTH = "health";
    public static final String ICON_CATEGORY_HOME = "home";
    public static final String ICON_CATEGORY_LIFESTYLE = "lifestyle";
    public static final String ICON_CATEGORY_MAPS = "maps";
    public static final String ICON_CATEGORY_MUSIC = "music";
    public static final String ICON_CATEGORY_NEWS = "news";
    public static final String ICON_CATEGORY_OTHER = "other";
    public static final String ICON_CATEGORY_PHOTOS = "photos";
    public static final String ICON_CATEGORY_SHOPPING = "shopping";
    public static final String ICON_CATEGORY_SOCIAL = "social";
    public static final String ICON_CATEGORY_SPORTS = "sports";
    public static final String ICON_CATEGORY_TRAVEL = "travel";
    public static final String ICON_CATEGORY_UTILITIES = "utilities";
    public static final String ICON_CATEGORY_VIDEO = "video";

    /** Lists the possible values for {@link #getIconCategory()}. */
    @SuppressWarnings("WeakerAccess")
    @StringDef({
            ICON_CATEGORY_BUSINESS, ICON_CATEGORY_CARS, ICON_CATEGORY_COMMUNICATION,
            ICON_CATEGORY_EDUCATION, ICON_CATEGORY_EVENTS, ICON_CATEGORY_FOOD,
            ICON_CATEGORY_GAMES, ICON_CATEGORY_HEALTH, ICON_CATEGORY_HOME, ICON_CATEGORY_LIFESTYLE,
            ICON_CATEGORY_MAPS, ICON_CATEGORY_MUSIC, ICON_CATEGORY_NEWS, ICON_CATEGORY_OTHER,
            ICON_CATEGORY_PHOTOS, ICON_CATEGORY_SHOPPING, ICON_CATEGORY_SOCIAL, ICON_CATEGORY_SPORTS,
            ICON_CATEGORY_TRAVEL, ICON_CATEGORY_UTILITIES, ICON_CATEGORY_VIDEO
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface IconCategory {}

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
    private static final String LINK_ANDROID_SHORTCUT_ID_KEY = "android_shortcut_id";
    private static final String LINK_ICON_CATEGORY = "icon_category";
    private static final String LINK_DEEPVIEW_EXTRA_TEXT_KEY = "deepview_extra_text";

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
    String web_link; /* read by BranchResponseParser */
    private String destination_store_id;
    private String click_tracking_url;
    private String android_shortcut_id;
    private String icon_category;
    String deepview_extra_text; /* read by BranchDeepViewFragment */

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

    @NonNull
    public String getRankingHint() {
        return ranking_hint;
    }

    /**
     * Returns true if this link represents an ad.
     * @return true if ad, false otherwise
     */
    @SuppressWarnings("unused")
    public boolean isAd() {
        return ranking_hint.toLowerCase().startsWith("featured");
    }

    public String getRoutingMode() {
        return routing_mode;
    }

    /**
     * If present, returns an Uri backed by an app-specific scheme that can
     * deep link into the app.
     * @return uri or null
     */
    @SuppressWarnings("WeakerAccess")
    @Nullable
    public String getUriScheme() {
        return TextUtils.isEmpty(uri_scheme) ? null : uri_scheme;
    }

    @SuppressWarnings("WeakerAccess")
    @NonNull
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
     * Returns the shortcut id, or null if this link does not represent an Android launcher shortcut.
     * To inspect the package, please see {@link #getDestinationPackageName()}.
     * @return id or null
     */
    @SuppressWarnings("WeakerAccess")
    @Nullable
    public String getAndroidShortcutId() {
        return TextUtils.isEmpty(android_shortcut_id) ? null : android_shortcut_id;
    }

    /**
     * Returns the icon category, one of the {@code ICON_CATEGORY_} constants in this class.
     * @return the icon category for this result
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    @IconCategory
    @NonNull
    public String getIconCategory() {
        return icon_category;
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
    @SuppressWarnings({"unused", "UnusedReturnValue"})
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
    @SuppressWarnings("UnusedReturnValue")
    @Nullable
    public BranchSearchError openContent(@NonNull Context context, boolean fallbackToPlayStore) {
        registerClickEvent();

        // 1. Try to open the app as an Android shortcut
        boolean success = openAppWithAndroidShortcut(context);

        // 2. Try to open the app directly with URI Scheme
        if (!success) {
            success = openAppWithUriScheme(context);
        }

        // 3. Try opening with the web link in app
        if (!success) {
            success = openAppWithWebLink(context, true);
        }

        // 4. Try opening with the web link in browser
        if (!success) {
            success = openAppWithWebLink(context, false);
        }

        // 5. Fallback to the playstore
        if (!success && fallbackToPlayStore) {
            success = openAppWithPlayStore(context);
        }

        BranchSearchError err = null;
        if (!success) {
            err = new BranchSearchError(BranchSearchError.ERR_CODE.ROUTING_ERR_UNABLE_TO_OPEN_APP);
        }
        return err;
    }

    /**
     * Tries to open this result as an Android shortcut, assuming it represents
     * one and the current {@link IBranchShortcutHandler} can handle it.
     * @param context a context
     * @return true if succeeded
     */
    private boolean openAppWithAndroidShortcut(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < 25) return false;
        String id = getAndroidShortcutId();
        if (id == null) return false;
        IBranchShortcutHandler handler = BranchSearch.getInstance()
                .getBranchConfiguration()
                .getShortcutHandler();
        return handler.launchShortcut(context, id, destination_store_id);
    }

    /**
     * Tries to open this result with the uri scheme, if present.
     * @param context a context
     * @return true if succeeded
     */
    @SuppressWarnings("StatementWithEmptyBody")
    private boolean openAppWithUriScheme(@NonNull Context context) {
        try {
            if (!TextUtils.isEmpty(uri_scheme)) {
                int intentFlags = BranchSearch.getInstance()
                        .getBranchConfiguration()
                        .getLaunchIntentFlags();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(uri_scheme);
                intent.setData(uri);
                intent.setFlags(intentFlags);
                if ("android-app".equals(uri.getScheme())) {
                    // Do not force the package! This is a special scheme
                    // defined by Android that contains the package in the URI itself.
                    // If app is not installed, the system should be free to handle this,
                    // typically by going to the play store.
                } else {
                    // This is an app-specific URI. Enforce the package name,
                    // just in the case of conflict between multiple apps using the same
                    // scheme (we've seen this happening). This will avoid the app chooser.
                    intent.setPackage(getDestinationPackageName());
                }
                context.startActivity(intent);
                return true;
            }
        } catch (Exception ignore) {
            // Nothing to do
        }
        return false;
    }

    /**
     * Tries to open this result with the web link.
     * If forcePackage is true, the package is passed to the intent to avoid the app chooser.
     * This can fail if the app does not support this link. If forcePackage is false, no
     * package is set and the browser (or the app chooser) might be launched.
     * @param context context
     * @param forcePackage true to force package
     * @return true if succeeded
     */
    private boolean openAppWithWebLink(@NonNull Context context, boolean forcePackage) {
        try {
            String webLink = getWebLink();
            if (!TextUtils.isEmpty(webLink)) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(webLink));
                if (forcePackage) {
                    intent.setPackage(getDestinationPackageName());
                }
                context.startActivity(intent);
                return true;
            }
        } catch (Exception ignore) {
            // Nothing to do
        }
        return false;
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

    @SuppressLint("NewApi")
    @NonNull
    static BranchLinkResult createFromJson(@NonNull JSONObject actionJson,
                                           @NonNull String appName,
                                           @NonNull String appStoreId,
                                           @NonNull String appIconUrl,
                                           @NonNull String appDeepviewExtraText) {
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
        if (link.metadata == null) link.metadata = new JSONObject();

        link.routing_mode = Util.optString(actionJson, LINK_ROUTING_MODE_KEY);
        link.uri_scheme = Util.optString(actionJson, LINK_URI_SCHEME_KEY);
        link.web_link = Util.optString(actionJson, LINK_WEB_LINK_KEY);
        link.destination_store_id = appStoreId;

        link.click_tracking_url = Util.optString(actionJson, LINK_TRACKING_KEY);
        link.android_shortcut_id = Util.optString(actionJson, LINK_ANDROID_SHORTCUT_ID_KEY);
        link.icon_category = actionJson.optString(LINK_ICON_CATEGORY, ICON_CATEGORY_OTHER);
        link.deepview_extra_text = actionJson.optString(LINK_DEEPVIEW_EXTRA_TEXT_KEY,
                appDeepviewExtraText);
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
        dest.writeString(this.android_shortcut_id);
        dest.writeString(this.icon_category);
        dest.writeString(this.deepview_extra_text);
    }


    private BranchLinkResult(@NonNull Parcel in) {
        this.entity_id = in.readString();
        this.type = in.readString();
        this.score = in.readFloat();

        this.name = in.readString();
        this.description = in.readString();
        this.image_url = in.readString();
        this.app_name = in.readString();
        this.app_icon_url = in.readString();
        this.ranking_hint = in.readString();
        try {
            this.metadata = new JSONObject(in.readString());
        } catch (JSONException e) {
            this.metadata = new JSONObject();
        }

        this.routing_mode = in.readString();
        this.uri_scheme = in.readString();
        this.web_link = in.readString();
        this.destination_store_id = in.readString();

        this.click_tracking_url = in.readString();
        this.android_shortcut_id = in.readString();
        this.icon_category = in.readString();
        this.deepview_extra_text = in.readString();
    }

    public static final Creator<BranchLinkResult> CREATOR = new Creator<BranchLinkResult>() {
        @Override
        public BranchLinkResult createFromParcel(Parcel source) {
            return new BranchLinkResult(source);
        }

        @Override
        public BranchLinkResult[] newArray(int size) {
            return new BranchLinkResult[size];
        }
    };

}
