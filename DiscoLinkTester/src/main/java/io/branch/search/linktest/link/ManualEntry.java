package io.branch.search.linktest.link;

import android.support.v7.app.AppCompatActivity;

import io.branch.search.linktest.R;
import io.branch.search.linktest.util.BranchTextEntry;

public class ManualEntry implements IBranchLinkProvider {

    private BranchTextEntry packageIdEntry;
    private BranchTextEntry developerShorcutEntry;
    private BranchTextEntry uriSchemeEntry;
    private BranchTextEntry webLinkEntry;

    public ManualEntry(AppCompatActivity activity) {
        packageIdEntry = (BranchTextEntry) activity.getSupportFragmentManager().findFragmentById(R.id.packageIdEntry);
        developerShorcutEntry = (BranchTextEntry) activity.getSupportFragmentManager().findFragmentById(R.id.developerShortcutEntry);
        uriSchemeEntry = (BranchTextEntry) activity.getSupportFragmentManager().findFragmentById(R.id.uriSchemeEntry);
        webLinkEntry = (BranchTextEntry) activity.getSupportFragmentManager().findFragmentById(R.id.webLinkEntry);

        packageIdEntry.setHeader("Package ID");
        developerShorcutEntry.setHeader("Developer Shortcut");
        uriSchemeEntry.setHeader("URI Scheme");
        webLinkEntry.setHeader("Web Link");
    }

    public void forceUpdate(Link link) {
        packageIdEntry.setValues(link.getPackageId());
        developerShorcutEntry.setValues(link.getAndroidShortcutId());
        uriSchemeEntry.setValues(link.getUriScheme());
        webLinkEntry.setValues(link.getWebLink());
    }

    @Override
    public String getPackageId() {
        return packageIdEntry.getEnteredValue();
    }

    @Override
    public String getAndroidShortcutId() {
        return developerShorcutEntry.getEnteredValue();
    }

    @Override
    public String getUriScheme() {
        return uriSchemeEntry.getEnteredValue();
    }

    @Override
    public String getWebLink() {
        return webLinkEntry.getEnteredValue();
    }
}
