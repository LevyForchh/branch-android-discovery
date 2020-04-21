package io.branch.search;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.os.Build;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import java.util.List;

/**
 * Handles {@link BranchDeepViewFragment} launch.
 */
public interface IBranchDeepViewHandler {

    /**
     * Launches the given deepview.
     * @param context context
     * @param fragment fragment
     * @return true if launched correctly
     */
    boolean launchDeepView(@NonNull Context context, @NonNull BranchDeepViewFragment fragment);

    /**
     * The default handler.
     */
    IBranchDeepViewHandler DEFAULT = new IBranchDeepViewHandler() {

        @Override
        public boolean launchDeepView(@NonNull Context context, @NonNull BranchDeepViewFragment fragment) {
            Activity activity = findActivity(context);
            if (activity == null) {
                return false;
            } else if (activity instanceof FragmentActivity) {
                FragmentManager manager = ((FragmentActivity) activity).getSupportFragmentManager();
                fragment.getInstance().show(manager, BranchDeepViewFragment.TAG);
            } else {
                // Legacy version of activities that do not extend FragmentActivity.
                android.app.FragmentManager manager = activity.getFragmentManager();
                fragment.getLegacyInstance().show(manager, BranchDeepViewFragment.TAG);
            }
            return false;
        }


        @Nullable
        private Activity findActivity(@NonNull Context context) {
            if (context instanceof Activity) {
                return (Activity) context;
            } else if (context instanceof ContextWrapper) {
                Context parent = ((ContextWrapper) context).getBaseContext();
                if (parent == null || parent == context) {
                    return null;
                } else {
                    return findActivity(parent);
                }
            } else {
                return null;
            }
        }
    };
}
