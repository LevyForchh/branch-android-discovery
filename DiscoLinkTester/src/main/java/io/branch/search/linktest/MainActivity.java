package io.branch.search.linktest;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

import io.branch.search.BranchLinkResult;
import io.branch.search.BranchSearch;
import io.branch.search.BranchSearchError;
import io.branch.search.linktest.link.Link;
import io.branch.search.linktest.link.ManualEntry;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "DiscoLinkTester";

    private ManualEntry manualEntry;
    private TextView lastRun;
    private CheckBox doSuppressExceptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        manualEntry = new ManualEntry(this);
        lastRun = findViewById(R.id.lastRunValue);
        doSuppressExceptions = findViewById(R.id.doSuppressExceptions);

        // Initialize the Branch Search SDK
        BranchSearch searchSDK = BranchSearch.init(getApplicationContext());
        if (searchSDK == null) {
            Toast.makeText(this, R.string.sdk_not_initialized, Toast.LENGTH_LONG).show();
            finish();
        }

        Button fireLink = findViewById(R.id.openContent);
        fireLink.setOnClickListener(this);

        final List<Link> precannedLinks = Arrays.asList(
                // PLAY STORE
                new Link("com.android.vending", "VIEW_MY_DOWNLOADS", null, null),
                new Link("com.android.vending", null, "android-app://com.android.vending", "android-app://com.android.vending"),
                new Link("com.android.vending", null, null, "https://play.google.com/store/apps/top"),
                // YOUTUBE
                new Link("com.google.android.youtube", "search-shortcut", null, null),
                new Link("com.google.android.youtube", null, null, "https://uber.com"),
                // Spotify
                new Link("com.spotify.music", "search", null, null),
                // UBER
                new Link("com.ubercab", null, null, "android-app://com.ubercab"),
                new Link("com.ubercab", null, "android-app://com.ubercab", null),
                // Flipkart
                new Link("com.flipkart.android", null, null, "android-app://com.flipkart.android"),
                new Link("com.flipkart.android", null, "android-app://com.flipkart.android", null),
                // Zomato
                new Link("com.application.zomato", null, null, "android-app://com.application.zomato"),
                new Link("com.application.zomato", null, "android-app://com.application.zomato", null)
        );

        final ListView listview = findViewById(R.id.precannedLinkList);
        listview.setAdapter(new ArrayAdapter<Link>(this,
                android.R.layout.simple_list_item_2, android.R.id.text1, precannedLinks) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);

                text1.setText(precannedLinks.get(position).getPackageId());
                text2.setText(precannedLinks.get(position).forDisplay());
                return view;
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Link item = (Link) parent.getItemAtPosition(position);
                manualEntry.forceUpdate(item);
            }
        });
    }

    public void startActivityForResult(Intent intent, int requestCode, Bundle bundle) {
        if (doSuppressExceptions.isChecked()) {
            try {
                super.startActivityForResult(intent, requestCode, bundle);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getApplicationContext(), "Activity not found!", Toast.LENGTH_SHORT).show();
            } catch (SecurityException e) {
                Toast.makeText(getApplicationContext(), "Security error!", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.startActivityForResult(intent, requestCode, bundle);
        }
    }

    @Override
    public void onClick(View v) {
        Parcel p = Parcel.obtain();
        // This has to line up with BranchLinkResult(Parcel in)

        // Junk we don't care about for testing links
        p.writeString("");
        p.writeString("");
        p.writeFloat(1);

        p.writeString("");
        p.writeString("");
        p.writeString("");
        p.writeString("");
        p.writeString("");
        p.writeString("{}");
        p.writeString("");

        // Links
        p.writeString(""); // Routing mode — not used anymore
        p.writeString(manualEntry.getUriScheme());
        p.writeString(manualEntry.getWebLink());
        p.writeString(manualEntry.getPackageId());
        p.writeString(""); // click tracking url — not testing here
        p.writeString(manualEntry.getAndroidShortcutId());

        p.setDataPosition(0);

        BranchLinkResult result = BranchLinkResult.CREATOR.createFromParcel(p);

        BranchSearchError res = result.openContent(this, true);

        String message = TextUtils.join("\n", Arrays.asList(
                "Error -> " + (res == null ? res : res.getErrorMsg()),
                "package -> " + result.getDestinationPackageName(),
                "dev shortcut -> " + result.getAndroidShortcutId(),
                "uri scheme -> " + result.getUriScheme(),
                "web link -> " + result.getWebLink()
        ));

        lastRun.setText("\n" + message);
    }
}
