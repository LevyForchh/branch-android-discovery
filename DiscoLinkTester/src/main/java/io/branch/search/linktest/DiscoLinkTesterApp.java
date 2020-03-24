package io.branch.search.linktest;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

import io.branch.search.BranchSearch;
import io.branch.search.BranchServiceEnabledResult;
import io.branch.search.IBranchServiceEnabledEvents;

public class DiscoLinkTesterApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        BranchSearch.isServiceEnabled(this, new IBranchServiceEnabledEvents() {
            @Override
            public void onBranchServiceEnabledResult(@NonNull BranchServiceEnabledResult result) {
                Log.d("DiscoLinkTesterApp", "Got service enabled result: " + result.isEnabled());
            }
        });
    }
}
