package io.branch.search;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by sojanpr on 3/16/18.
 * <p>
 * Class for parsing a Branch search response
 * </p>
 */

class BranchResponseParser {

    private static final String REQUEST_ID_KEY = "request_id";
    private static final String RESULTS_KEY = "results";
    private static final String SUCCESS_KEY = "success";
    private static final String CORRECTED_QUERY_KEY = "search_query_string";

    private static final String APP_NAME_KEY = "app_name";
    private static final String APP_STORE_ID_KEY = "app_store_id";
    private static final String APP_ICON_URL_KEY = "app_icon_url";
    private static final String APP_SCORE_KEY = "score";
    private static final String APP_DEEP_LINKS_KEY = "deep_links";
    private static final String RANKING_HINT_KEY = "ranking_hint";
    private static final String UNINSTALLED_MAX_RESULTS_KEY = "uninstalled_max_results";


    static BranchSearchResult parse(BranchSearchRequest query, JSONObject object) {
        String corrected_query = null;
        if (object.optString(CORRECTED_QUERY_KEY, null) != null) {
            corrected_query = object.optString(CORRECTED_QUERY_KEY);
        }

        BranchSearchResult branchSearchResult = new BranchSearchResult(query, corrected_query);
        // Return if request is not succeeded
        if (object.optBoolean(SUCCESS_KEY)) {
            JSONArray resultsArray = object.optJSONArray(RESULTS_KEY);
            if (resultsArray != null) {
                parseResultArray(Util.optString(object, REQUEST_ID_KEY),
                        resultsArray,
                        branchSearchResult);
            }
        }
        return branchSearchResult;
    }

    private static void parseResultArray(@NonNull String requestID,
                                         @NonNull JSONArray resultsArray,
                                         @NonNull BranchSearchResult branchSearchResult) {
        for (int i = 0; i < resultsArray.length(); i++) {
            JSONObject resultObj = resultsArray.optJSONObject(i);
            if (resultObj == null) continue;

            // Parse fields and check if installed
            String name = Util.optString(resultObj, APP_NAME_KEY);
            String store_id = Util.optString(resultObj, APP_STORE_ID_KEY);
            String icon_url = Util.optString(resultObj, APP_ICON_URL_KEY);
            float score = (float)resultObj.optDouble(APP_SCORE_KEY, 0.0);
            String rankingHint = Util.optString(resultObj, RANKING_HINT_KEY);
            Context appContext = BranchSearch.getInstance().getApplicationContext();
            boolean isInstalled = Util.isAppInstalled(appContext, store_id);

            // Parse deep links
            JSONArray rawDeepLinks = resultObj.optJSONArray(APP_DEEP_LINKS_KEY);
            List<BranchLinkResult> deepLinks = new ArrayList<>();
            if (rawDeepLinks != null) {
                for (int j = 0; j < rawDeepLinks.length(); j++) {
                    BranchLinkResult link = BranchLinkResult.createFromJson(
                            rawDeepLinks.optJSONObject(j),
                            name,
                            store_id,
                            icon_url);
                    deepLinks.add(link);
                }
            }

            // Filter results
            Iterator<BranchLinkResult> linkIterator = deepLinks.listIterator();
            while (linkIterator.hasNext()) {
                BranchLinkResult link = linkIterator.next();

                // Remove invalid shortcuts
                String shortcutId = link.getAndroidShortcutId();
                if (shortcutId != null) { // Need to validate
                    IBranchShortcutHandler handler = BranchSearch.getInstance()
                            .getBranchConfiguration()
                            .getShortcutHandler();
                    if (!handler.validateShortcut(appContext, shortcutId, store_id)) {
                        linkIterator.remove();
                        continue;
                    }
                }

                // If app not installed, remove non http(s)/android-app.
                if (!isInstalled) {
                    boolean isWeb = !TextUtils.isEmpty(link.web_link);
                    boolean isPlayStore = link.getUriScheme() != null
                            && link.getUriScheme().startsWith("android-app://");
                    if (!isWeb && !isPlayStore) {
                        linkIterator.remove();
                    }
                }
            }

            // Apply the max results constraint
            // If nothing remains, this app should disappear
            if (!isInstalled) {
                int maxResults = resultObj.optInt(UNINSTALLED_MAX_RESULTS_KEY, Integer.MAX_VALUE);
                int max = Math.min(maxResults, deepLinks.size());
                deepLinks = deepLinks.subList(0, max);
            }
            if (deepLinks.isEmpty()) {
                continue;
            }

            // Create BranchAppResult and return
            BranchAppResult appResult = new BranchAppResult(store_id,
                    name,
                    icon_url,
                    rankingHint,
                    score,
                    deepLinks);
            branchSearchResult.results.add(appResult);
        }
    }
}
