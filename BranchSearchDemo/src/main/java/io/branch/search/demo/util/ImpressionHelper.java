package io.branch.search.demo.util;

import android.content.res.Resources;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.branch.search.BranchLinkResult;

public class ImpressionHelper {
    private Timer timer = new Timer();

    public ImpressionHelper(final View view, final BranchLinkResult result) {
        final ImpressionHelper this_ = this;
        timer.scheduleAtFixedRate(new TimerTask() {

            public void run() {
                if (isVisible(view)) {
                    Log.w("ImpressionHelper", "Got impression for " + result.getEntityID() + " !!!!");
                    this_.timer.cancel();
                    this_.timer = null;
                } else {
                    //Log.w("ImpressionHelper", "NO " + result.getEntityID());
                }
            }

        }, 0, TimeUnit.SECONDS.toMillis(1));

    }

    private static boolean isVisible(final View view) {
        // Modified from https://stackoverflow.com/a/49038086/11876026
        // in order to take account of the keyboard

        if (view == null) {
            return false;
        }
        if (!view.isShown()) {
            return false;
        }

        final Rect actualElementPosition = new Rect();
        view.getGlobalVisibleRect(actualElementPosition);

        // This rect respects if the keyboard is up or not.
        final Rect visibleWindowArea = new Rect();
        view.getWindowVisibleDisplayFrame(visibleWindowArea);

        // TODO: add a buffer because if only 1% of the entity is visible then it's not an impression

        return actualElementPosition.intersect(visibleWindowArea);
    }
}
