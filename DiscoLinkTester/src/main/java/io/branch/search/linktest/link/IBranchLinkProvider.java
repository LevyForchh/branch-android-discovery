package io.branch.search.linktest.link;

public interface IBranchLinkProvider {
    String getPackageId();

    String getAndroidShortcutId();
    String getUriScheme();
    String getWebLink();
}
