/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2015.
 */

package org.telegram.messenger;

public class BuildVars {
    public static boolean DEBUG_VERSION = false;
    public static int BUILD_VERSION = 5358; // increase all 3 of them, according to this pattern: (BuildVars.BUILD_VERSION = build.versionCode > R.string.updateBuild)
    public static int APP_ID = 39835; //obtain your own APP_ID at https://core.telegram.org/api/obtaining_api_id
    public static String APP_HASH = "2535354cf42e12d2bfac6c00ba949d09"; //obtain your own APP_HASH at https://core.telegram.org/api/obtaining_api_id
    public static String HOCKEY_APP_HASH = "2535354cf42e12d2bfac6c00ba949d09";
    public static String HOCKEY_APP_HASH_DEBUG = "2535354cf42e12d2bfac6c00ba949d09";
    public static String GCM_SENDER_ID = "1076345567071,760348033672";
    public static String SEND_LOGS_EMAIL = "7shayan7@gmail.com";
    public static String BING_SEARCH_KEY = ""; //obtain your own KEY at https://www.bing.com/dev/en-us/dev-center
    public static String FOURSQUARE_API_KEY = ""; //obtain your own KEY at https://developer.foursquare.com/
    public static String FOURSQUARE_API_ID = ""; //obtain your own API_ID at https://developer.foursquare.com/
    public static String FOURSQUARE_API_VERSION = "20150326";
}
