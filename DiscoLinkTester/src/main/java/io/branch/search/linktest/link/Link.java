package io.branch.search.linktest.link;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class Link implements IBranchLinkProvider {
    private String packageId;
    private String shortcutId;
    private String uriScheme;
    private String webLink;

    public Link(String packageId, String shortcutId, String uriScheme, String webLink) {
        this.packageId = packageId;
        this.shortcutId = shortcutId;
        this.uriScheme = uriScheme;
        this.webLink = webLink;
    }

    private static void prettyFormat(List<String> li, String name, String value) {
        if (value == null) {
            return;
        } else if ("".equals(value)) {
            return;
        } else {
            li.add(name + "='" + value + "'");
        }
    }

    @Override
    public String getPackageId() {
        return packageId;
    }

    @Override
    public String getAndroidShortcutId() {
        return shortcutId;
    }

    @Override
    public String getUriScheme() {
        return uriScheme;
    }

    @Override
    public String getWebLink() {
        return webLink;
    }

    public String forDisplay() {
        List<String> output = new ArrayList<>();
        prettyFormat(output, "shortcut", shortcutId);
        prettyFormat(output, "uri_scheme", uriScheme);
        prettyFormat(output, "web_link", webLink);
        return TextUtils.join(", ", output);
    }
}
