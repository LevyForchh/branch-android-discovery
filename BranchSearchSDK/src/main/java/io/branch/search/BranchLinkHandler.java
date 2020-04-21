package io.branch.search;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Abstract class that represents Link Handling v2 concepts. Both opening methods
 * and scoping rules will be instances of a {@link BranchLinkHandler}.
 *
 * Handlers can contain other handlers inside: see {@link Wrapper}.
 * In this case validation and opening functions will forward the event to children.
 *
 * All handler implementations are currently private and are meant to be constructed
 * from JSON using {@link BranchLinkHandler#from(JSONObject)}.
 * These classes also use the JSON string to easily implement {@link Parcelable}.
 */
abstract class BranchLinkHandler implements Parcelable {

    @NonNull
    static BranchLinkHandler from(@NonNull JSONObject payload) throws JSONException {
        String type = payload.getString("@type");
        switch (type) {
            case ViewIntent.TYPE: return new ViewIntent(payload);
            case LaunchIntent.TYPE: return new LaunchIntent(payload);
            case CustomIntent.TYPE: return new CustomIntent(payload);
            case Shortcut.TYPE: return new Shortcut(payload);
            case TestInstalled.TYPE: return new TestInstalled(payload);
            case TestNotInstalled.TYPE: return new TestNotInstalled(payload);
            case DeepView.TYPE: return new DeepView(payload);
            default: throw new JSONException("Unknown type!");
        }
    }

    private final String payloadString;

    @SuppressWarnings("WeakerAccess")
    protected BranchLinkHandler(@NonNull JSONObject payload) {
        payloadString = payload.toString();
    }

    /**
     * Called for the handler to validate its contents. If false is returned, this handler
     * will be discarded and {@link #open(Context, BranchLinkResult)} will not be called.
     * If true is returned, open might be called but it's still possible that it will fail.
     * @param context a valid context
     * @param parent the root result
     * @return true if probably openable
     */
    abstract boolean validate(@NonNull Context context, @NonNull BranchLinkResult parent);

    /**
     * Called for the handler to open its contents. Should return true if the action
     * succeeded, and false otherwise.
     * @param context a valid context
     * @param parent the root result
     * @return true if opened
     */
    abstract boolean open(@NonNull Context context, @NonNull BranchLinkResult parent);

    @Override
    public final void writeToParcel(Parcel dest, int flags) {
        dest.writeString(payloadString);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public final static Creator<BranchLinkHandler> CREATOR = new Creator<BranchLinkHandler>() {
        @Override
        public BranchLinkHandler createFromParcel(Parcel source) {
            try {
                return from(new JSONObject(source.readString()));
            } catch (JSONException e) {
                throw new RuntimeException("Can't happen!");
            }
        }

        @Override
        public BranchLinkHandler[] newArray(int size) {
            return new BranchLinkHandler[size];
        }
    };

    /**
     * Base class for intent-based handlers.
     * See {@link ViewIntent}, {@link LaunchIntent}, {@link CustomIntent}.
     */
    @SuppressWarnings("WeakerAccess")
    private static abstract class BaseIntent extends BranchLinkHandler {
        private final static String KEY_EXTRAS = "extras";

        private final Map<String, String> extras = new HashMap<>();

        protected BaseIntent(@NonNull JSONObject payload) throws JSONException {
            super(payload);
            if (payload.has(KEY_EXTRAS)) {
                JSONObject extras = payload.getJSONObject(KEY_EXTRAS);
                Iterator<String> keys = extras.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    this.extras.put(key, extras.getString(key));
                }
            }
        }

        @Nullable
        protected abstract Intent createIntent(@NonNull Context context, @NonNull BranchLinkResult parent);

        @Override
        boolean validate(@NonNull Context context, @NonNull BranchLinkResult parent) {
            Intent intent = createIntent(context, parent);
            PackageManager manager = context.getPackageManager();
            return intent != null && !manager.queryIntentActivities(intent, 0).isEmpty();
        }

        @Override
        boolean open(@NonNull Context context, @NonNull BranchLinkResult parent) {
            Intent intent = createIntent(context, parent);
            if (intent != null) {
                for (String extra : extras.keySet()) {
                    intent.putExtra(extra, extras.get(extra));
                }
                intent.setFlags(BranchSearch.getInstance()
                        .getBranchConfiguration()
                        .getLaunchIntentFlags());
                try {
                    context.startActivity(intent);
                    return true;
                } catch (Exception ignore) {}
            }
            return false;
        }
    }

    /**
     * The "view_intent" handler, opening an Uri and optionally forcing the target package.
     */
    private static class ViewIntent extends BaseIntent {
        private final static String TYPE = "view_intent";
        private final static String KEY_DATA = "data";
        private final static String KEY_TARGET = "forcePackage";

        private final Uri data;
        private final String target;

        private ViewIntent(@NonNull JSONObject payload) throws JSONException {
            super(payload);
            data = Uri.parse(payload.getString(KEY_DATA));
            target = payload.has(KEY_TARGET) ? payload.getString(KEY_TARGET) : null;
        }

        @Nullable
        @Override
        protected Intent createIntent(@NonNull Context context, @NonNull BranchLinkResult parent) {
            Intent intent = new Intent(Intent.ACTION_VIEW, data);
            if (target != null) intent.setPackage(target);
            return intent;
        }
    }

    /**
     * The "intent" handler, that creates intents with a specific action and optional Uri.
     * Action is mandatory.
     */
    private static class CustomIntent extends BaseIntent {
        private final static String TYPE = "intent";
        private final static String KEY_DATA = "data";
        private final static String KEY_ACTION = "action";

        private final Uri data;
        private final String action;

        private CustomIntent(@NonNull JSONObject payload) throws JSONException {
            super(payload);
            data = payload.has(KEY_DATA) ? Uri.parse(payload.getString(KEY_DATA)) : null;
            action = payload.getString(KEY_ACTION);
        }

        @Nullable
        @Override
        protected Intent createIntent(@NonNull Context context, @NonNull BranchLinkResult parent) {
            Intent intent = new Intent(action);
            if (data != null) intent.setData(data);
            return intent;
        }
    }

    /**
     * The "launch_intent" handler, opening an app with
     * {@link android.content.pm.PackageManager#getLaunchIntentForPackage(String)}.
     */
    private static class LaunchIntent extends BaseIntent {
        private final static String TYPE = "launch_intent";

        private LaunchIntent(@NonNull JSONObject payload) throws JSONException {
            super(payload);
        }

        @Nullable
        @Override
        protected Intent createIntent(@NonNull Context context, @NonNull BranchLinkResult parent) {
            String packageName = parent.getDestinationPackageName();
            return context.getPackageManager().getLaunchIntentForPackage(packageName);
        }
    }

    /**
     * The "shortcut" handler, opening an Android shortcut if possible.
     * This actually delegates everything to the public {@link IBranchShortcutHandler}.
     */
    private static class Shortcut extends BranchLinkHandler {
        private final static String TYPE = "shortcut";
        private final static String KEY_ID = "id";

        private final String id;

        private Shortcut(@NonNull JSONObject payload) throws JSONException {
            super(payload);
            id = payload.getString(KEY_ID);
        }

        @Override
        boolean validate(@NonNull Context context, @NonNull BranchLinkResult parent) {
            String appPackageName = parent.getDestinationPackageName();
            IBranchShortcutHandler handler = BranchSearch.getInstance()
                    .getBranchConfiguration()
                    .getShortcutHandler();
            return handler.validateShortcut(context, id, appPackageName);
        }

        @Override
        boolean open(@NonNull Context context, @NonNull BranchLinkResult parent) {
            String appPackageName = parent.getDestinationPackageName();
            IBranchShortcutHandler handler = BranchSearch.getInstance()
                    .getBranchConfiguration()
                    .getShortcutHandler();
            return handler.launchShortcut(context, id, appPackageName);
        }
    }

    /**
     * Abstract handler that contains other handlers inside.
     */
    @SuppressWarnings("WeakerAccess")
    private abstract static class Wrapper extends BranchLinkHandler {
        private final static String KEY_HANDLERS = "links";

        protected final List<BranchLinkHandler> handlers = new ArrayList<>();

        protected Wrapper(@NonNull JSONObject payload) throws JSONException {
            super(payload);
            JSONArray array = payload.getJSONArray(KEY_HANDLERS);
            for (int i = 0; i < array.length(); i++) {
                JSONObject handler = array.getJSONObject(i);
                handlers.add(BranchLinkHandler.from(handler));
            }
        }

        @Override
        boolean validate(@NonNull Context context, @NonNull BranchLinkResult parent) {
            for (BranchLinkHandler handler : handlers) {
                if (handler.validate(context, parent)) return true;
            }
            return false;
        }

        @Override
        boolean open(@NonNull Context context, @NonNull BranchLinkResult parent) {
            for (BranchLinkHandler handler : handlers) {
                if (handler.open(context, parent)) return true;
            }
            return false;
        }
    }

    /**
     * A {@link Wrapper} that tests that the target package is installed before
     * passing the action to wrapped handlers.
     */
    private static class TestInstalled extends Wrapper {
        private final static String TYPE = "test_installed";
        private final static String KEY_PACKAGE = "package";

        private final String packageName;

        private TestInstalled(@NonNull JSONObject payload) throws JSONException {
            super(payload);
            packageName = payload.getString(KEY_PACKAGE);
        }

        @Override
        boolean validate(@NonNull Context context, @NonNull BranchLinkResult parent) {
            if (!Util.isAppInstalled(context, packageName)) return false;
            return super.validate(context, parent);
        }
    }

    /**
     * A {@link Wrapper} that tests that the target package is not installed before
     * passing the action to wrapped handlers.
     */
    private static class TestNotInstalled extends Wrapper {
        private final static String TYPE = "test_not_installed";
        private final static String KEY_PACKAGE = "package";

        private final String packageName;

        private TestNotInstalled(@NonNull JSONObject payload) throws JSONException {
            super(payload);
            packageName = payload.getString(KEY_PACKAGE);
        }

        @Override
        boolean validate(@NonNull Context context, @NonNull BranchLinkResult parent) {
            if (Util.isAppInstalled(context, packageName)) return false;
            return super.validate(context, parent);
        }
    }

    /**
     * A {@link Wrapper} that opens a {@link BranchDeepViewFragment} and passes it to the
     * deepview handler that can be registered by users. The default behavior will open the
     * fragment as a floating dialog. On click of the CTA button, this wrapper's handlers will
     * be invoked.
     */
    private static class DeepView extends Wrapper {
        private final static String TYPE = "deep_view";

        private final static String KEY_TITLE = "title";
        private final static String KEY_DESCRIPTION = "description";
        private final static String KEY_IMAGE_URL = "image_url";
        // TODO cta text ?
        // TODO put deepview_extra_text here instead of link level ?

        private final String imageUrl;
        private final String title;
        private final String description;

        private DeepView(@NonNull JSONObject payload) throws JSONException {
            super(payload);
            imageUrl = payload.optString(KEY_IMAGE_URL);
            title = payload.optString(KEY_TITLE);
            description = payload.optString(KEY_DESCRIPTION);
        }

        @Override
        boolean open(@NonNull Context context, @NonNull BranchLinkResult parent) {
            // Don't call super: these links must only be opened on click of the CTA button.
            // Instead, construct the fragment and let the public handler open it.
            IBranchDeepViewHandler handler = BranchSearch.getInstance()
                    .getBranchConfiguration()
                    .getDeepViewHandler();
            BranchDeepViewFragment fragment = new BranchDeepViewFragment(parent,
                    handlers,
                    TextUtils.isEmpty(title) ? parent.getName() : title,
                    TextUtils.isEmpty(description) ? parent.getDescription() : description,
                    TextUtils.isEmpty(imageUrl) ? parent.getImageUrl() : imageUrl,
                    parent.deepview_extra_text);
            return handler.launchDeepView(context, fragment);
        }
    }
}
