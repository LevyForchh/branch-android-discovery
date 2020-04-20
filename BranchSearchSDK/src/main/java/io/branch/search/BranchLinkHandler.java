package io.branch.search;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

abstract class BranchLinkHandler implements Parcelable {

    @NonNull
    static BranchLinkHandler from(@NonNull JSONObject payload) throws JSONException {
        String type = payload.getString("@type");
        switch (type) {
            case "view_intent": return new ViewIntent(payload);
            case "launch_intent": return new LaunchIntent(payload);
            case "shortcut": return new Shortcut(payload);
            default: throw new JSONException("Unknown type!");
        }
    }

    private final String payloadString;

    protected BranchLinkHandler(@NonNull JSONObject payload) {
        payloadString = payload.toString();
    }

    abstract boolean validate(@NonNull Context context, @NonNull String appPackageName);

    abstract boolean open(@NonNull Context context, @NonNull String appPackageName);

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
     * The view_intent handler, opening an Uri and optionally forcing the target package.
     */
    private static class ViewIntent extends BranchLinkHandler {
        private final static String KEY_DATA = "data";
        private final static String KEY_TARGET = "forcePackage";
        private final static String KEY_EXTRAS = "extras";

        private final Uri data;
        private final String target;
        private final Map<String, String> extras = new HashMap<>();

        private ViewIntent(@NonNull JSONObject payload) throws JSONException {
            super(payload);
            data = Uri.parse(payload.getString(KEY_DATA));
            target = payload.has(KEY_TARGET) ? payload.getString(KEY_TARGET) : null;
            if (payload.has(KEY_EXTRAS)) {
                JSONObject extras = payload.getJSONObject(KEY_EXTRAS);
                Iterator<String> keys = extras.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    this.extras.put(key, extras.getString(key));
                }
            }
        }

        @Override
        boolean validate(@NonNull Context context, @NonNull String appPackageName) {
            if (target != null && !Util.isAppInstalled(context, target)) return false;
            Intent intent = new Intent(Intent.ACTION_VIEW, data);
            if (target != null) intent.setPackage(target); // no need to add extras here.
            return !context.getPackageManager().queryIntentActivities(intent, 0).isEmpty();
        }

        @Override
        boolean open(@NonNull Context context, @NonNull String appPackageName) {
            Intent intent = new Intent(Intent.ACTION_VIEW, data);
            if (target != null) intent.setPackage(target);
            for (String extra : extras.keySet()) {
                intent.putExtra(extra, extras.get(extra));
            }
            intent.setFlags(BranchSearch.getInstance()
                    .getBranchConfiguration()
                    .getLaunchIntentFlags());
            try {
                context.startActivity(intent);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    /**
     * The launch_intent handler, opening an app with
     * {@link android.content.pm.PackageManager#getLaunchIntentForPackage(String)}.
     */
    private static class LaunchIntent extends BranchLinkHandler {
        private final static String KEY_EXTRAS = "extras";

        private final Map<String, String> extras = new HashMap<>();

        private LaunchIntent(@NonNull JSONObject payload) throws JSONException {
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

        @Override
        boolean validate(@NonNull Context context, @NonNull String appPackageName) {
            return context.getPackageManager().getLaunchIntentForPackage(appPackageName) != null;
        }

        @Override
        boolean open(@NonNull Context context, @NonNull String appPackageName) {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(appPackageName);
            if (intent == null) return false; // App uninstalled after validation!
            for (String extra : extras.keySet()) {
                intent.putExtra(extra, extras.get(extra));
            }
            // addFlags instead of setFlags, to ensure we don't override anything important.
            intent.addFlags(BranchSearch.getInstance()
                    .getBranchConfiguration()
                    .getLaunchIntentFlags());
            try {
                context.startActivity(intent);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    /**
     * The "shortcut" handler, opening an Android shortcut if possible.
     * This actually delegates everything to the public {@link IBranchShortcutHandler}.
     */
    private static class Shortcut extends BranchLinkHandler {
        private final static String KEY_ID = "id";

        private final String id;

        private Shortcut(@NonNull JSONObject payload) throws JSONException {
            super(payload);
            id = payload.getString(KEY_ID);
        }

        @Override
        boolean validate(@NonNull Context context, @NonNull String appPackageName) {
            IBranchShortcutHandler handler = BranchSearch.getInstance()
                    .getBranchConfiguration()
                    .getShortcutHandler();
            return handler.validateShortcut(context, id, appPackageName);
        }

        @Override
        boolean open(@NonNull Context context, @NonNull String appPackageName) {
            IBranchShortcutHandler handler = BranchSearch.getInstance()
                    .getBranchConfiguration()
                    .getShortcutHandler();
            return handler.launchShortcut(context, id, appPackageName);
        }
    }
}
