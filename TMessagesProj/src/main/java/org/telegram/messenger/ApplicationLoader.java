/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package org.telegram.messenger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.support.multidex.MultiDexApplication;
import android.util.Base64;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.onesignal.OSNotification;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OSNotificationPayload;
import com.onesignal.OneSignal;

import org.gramity.FontSelectActivity;
import org.gramity.GramityConstants;
import org.gramity.GramityUtilities;
import org.gramity.MarketHandlerActivity;
import org.gramity.database.DatabaseHandler;
import org.json.JSONObject;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.ForegroundDetector;
import org.telegram.ui.LaunchActivity;

import java.io.File;
import java.io.RandomAccessFile;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class ApplicationLoader extends MultiDexApplication {

    @SuppressLint("StaticFieldLeak")
    public static volatile Context applicationContext;
    public static volatile Handler applicationHandler;
    private static volatile boolean applicationInited = false;

    public static volatile boolean isScreenOn = false;
    public static volatile boolean mainInterfacePaused = true;

    //tgy
    public static DatabaseHandler databaseHandler;
    public static boolean KEEP_ORIGINAL_FILENAME;
    //

    public static volatile boolean mainInterfacePausedStageQueue = true;
    public static volatile long mainInterfacePausedStageQueueTime;

    private static void convertConfig() {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("dataconfig", Context.MODE_PRIVATE);
        if (preferences.contains("currentDatacenterId")) {
            SerializedData buffer = new SerializedData(32 * 1024);
            buffer.writeInt32(2);
            buffer.writeBool(preferences.getInt("datacenterSetId", 0) != 0);
            buffer.writeBool(true);
            buffer.writeInt32(preferences.getInt("currentDatacenterId", 0));
            buffer.writeInt32(preferences.getInt("timeDifference", 0));
            buffer.writeInt32(preferences.getInt("lastDcUpdateTime", 0));
            buffer.writeInt64(preferences.getLong("pushSessionId", 0));
            buffer.writeBool(false);
            buffer.writeInt32(0);
            try {
                String datacentersString = preferences.getString("datacenters", null);
                if (datacentersString != null) {
                    byte[] datacentersBytes = Base64.decode(datacentersString, Base64.DEFAULT);
                    if (datacentersBytes != null) {
                        SerializedData data = new SerializedData(datacentersBytes);
                        buffer.writeInt32(data.readInt32(false));
                        buffer.writeBytes(datacentersBytes, 4, datacentersBytes.length - 4);
                        data.cleanup();
                    }
                }
            } catch (Exception e) {
                FileLog.e(e);
            }

            try {
                File file = new File(getFilesDirFixed(), "tgnet.dat");
                RandomAccessFile fileOutputStream = new RandomAccessFile(file, "rws");
                byte[] bytes = buffer.toByteArray();
                fileOutputStream.writeInt(Integer.reverseBytes(bytes.length));
                fileOutputStream.write(bytes);
                fileOutputStream.close();
            } catch (Exception e) {
                FileLog.e(e);
            }
            buffer.cleanup();
            preferences.edit().clear().commit();
        }
    }

    public static File getFilesDirFixed() {
        for (int a = 0; a < 10; a++) {
            File path = ApplicationLoader.applicationContext.getFilesDir();
            if (path != null) {
                return path;
            }
        }
        try {
            ApplicationInfo info = applicationContext.getApplicationInfo();
            File path = new File(info.dataDir, "files");
            path.mkdirs();
            return path;
        } catch (Exception e) {
            FileLog.e(e);
        }
        return new File("/data/data/org.telegram.messenger/files");
    }

    public static void postInitApplication() {
        if (applicationInited) {
            return;
        }

        applicationInited = true;
        convertConfig();

        try {
            LocaleController.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            final BroadcastReceiver mReceiver = new ScreenReceiver();
            applicationContext.registerReceiver(mReceiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            PowerManager pm = (PowerManager) ApplicationLoader.applicationContext.getSystemService(Context.POWER_SERVICE);
            isScreenOn = pm.isScreenOn();
            FileLog.e("screen state = " + isScreenOn);
        } catch (Exception e) {
            FileLog.e(e);
        }

        UserConfig.loadConfig();
        String deviceModel;
        String langCode;
        String appVersion;
        String systemVersion;
        String configPath = getFilesDirFixed().toString();

        try {
            langCode = LocaleController.getLocaleStringIso639();
            deviceModel = Build.MANUFACTURER + Build.MODEL;
            PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
            appVersion = pInfo.versionName + " (" + pInfo.versionCode + ")";
            systemVersion = "SDK " + Build.VERSION.SDK_INT;
        } catch (Exception e) {
            langCode = "en";
            deviceModel = "Android unknown";
            appVersion = "App version unknown";
            systemVersion = "SDK " + Build.VERSION.SDK_INT;
        }
        if (langCode.trim().length() == 0) {
            langCode = "en";
        }
        if (deviceModel.trim().length() == 0) {
            deviceModel = "Android unknown";
        }
        if (appVersion.trim().length() == 0) {
            appVersion = "App version unknown";
        }
        if (systemVersion.trim().length() == 0) {
            systemVersion = "SDK Unknown";
        }

        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
        boolean enablePushConnection = preferences.getBoolean("pushConnection", true);

        MessagesController.getInstance();
        ConnectionsManager.getInstance().init(BuildVars.BUILD_VERSION, TLRPC.LAYER, BuildVars.APP_ID, deviceModel, systemVersion, appVersion, langCode, configPath, FileLog.getNetworkLogPath(), UserConfig.getClientUserId(), enablePushConnection);
        if (UserConfig.getCurrentUser() != null) {
            MessagesController.getInstance().putUser(UserConfig.getCurrentUser(), true);
            ConnectionsManager.getInstance().applyCountryPortNumber(UserConfig.getCurrentUser().phone);
            MessagesController.getInstance().getBlockedUsers(true);
            SendMessagesHelper.getInstance().checkUnsentMessages();
        }

        ApplicationLoader app = (ApplicationLoader) ApplicationLoader.applicationContext;
        app.initPlayServices();
        FileLog.e("app initied");

        ContactsController.getInstance().checkAppAccount();
        MediaController.getInstance();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        applicationContext = getApplicationContext();
        NativeLoader.initNativeLibs(ApplicationLoader.applicationContext);
        ConnectionsManager.native_setJava(Build.VERSION.SDK_INT == 14 || Build.VERSION.SDK_INT == 15);
        new ForegroundDetector(this);

        applicationHandler = new Handler(applicationContext.getMainLooper());

        //GTY
        databaseHandler = new DatabaseHandler(applicationContext);
        SharedPreferences advancedPreferences = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE);
        KEEP_ORIGINAL_FILENAME = advancedPreferences.getBoolean("keepOriginalFilename", true);
        //

        OneSignal.startInit(this)
                .setNotificationOpenedHandler(new ApplicationLoader.TGYNotificationOpenedHandler())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .autoPromptLocation(true)
                .init();

        OneSignal.deleteTag("market");
        OneSignal.deleteTag("store");
        if (!ApplicationLoader.applicationContext.getPackageName().equals(GramityConstants.TGPPKG)) {
            if (GramityUtilities.isPackageInstalled(GramityConstants.BAZAAR_PKG)) {
                OneSignal.sendTag("store", "Bazaar");
            } else if (GramityUtilities.isPackageInstalled(GramityConstants.PLAY_PKG)) {
                OneSignal.sendTag("store", "Play");
            }
        } else if (GramityUtilities.isPackageInstalled(GramityConstants.AVVAL_PKG)) {
            OneSignal.sendTag("store", "Avval");
        }

        startPushService();

        String customAssetPath = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Activity.MODE_PRIVATE).getString(GramityConstants.PREF_CUSTOM_FONT_PATH, FontSelectActivity.DEFAULT_FONT_PATH);
        if (!customAssetPath.equals("device")) {
            CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                    .setDefaultFontPath(customAssetPath)
                    .setFontAttrId(R.attr.fontPath)
                    .build()
            );
        }
    }

    private class TGYNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
        @Override
        public void notificationOpened(OSNotificationOpenResult openResult) {
            OSNotification notification = openResult.notification;
            OSNotificationPayload payload = notification.payload;
            JSONObject additionalData = payload.additionalData;
            try {
                if (additionalData != null) {
                    if (additionalData.has("channel") && additionalData.getString("channel") != null) {
                        BaseFragment fragment = LaunchActivity.getMainFragment();
                        if (fragment != null) {
                            GramityUtilities.snkrGApnr(additionalData.getString("channel"), fragment, true, false);
                        }
                    }
                    if (additionalData.has("appMarket") && additionalData.getString("appMarket") != null) {
                        MarketHandlerActivity.openAppPage(additionalData.getString("appMarket"));
                    }
                    if (additionalData.has("rateMarket")) {
                        try {
                            PendingIntent.getActivity(ApplicationLoader.this, 0, new Intent(ApplicationLoader.this, MarketHandlerActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP), 0).send();
                        } catch (Exception e) {
                            FileLog.e("Error starting app rating activity", e);
                        }
                    }
                    if (additionalData.has("link") && additionalData.getString("link") != null) {
                        Intent linkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(additionalData.getString("link")));
                        startActivity(linkIntent);
                    }
                    /*if (additionalData.has("iab")) { // TGY ad
                        purchasePresenter();
                    }*/
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /*private void purchasePresenter() { // TGY ad
        actionBarLayout.presentFragment(new PremiumActivity(), false, true, true);
        if (AndroidUtilities.isTablet()) {
            actionBarLayout.showLastFragment();
            rightActionBarLayout.showLastFragment();
            drawerLayoutContainer.setAllowOpenDrawer(false, false);
        } else {
            drawerLayoutContainer.setAllowOpenDrawer(true, false);
        }
    }*/

    /*public static void sendRegIdToBackend(final String token) {
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                UserConfig.pushString = token;
                UserConfig.registeredForPush = false;
                UserConfig.saveConfig(false);
                if (UserConfig.getClientUserId() != 0) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            MessagesController.getInstance().registerForPush(token);
                        }
                    });
                }
            }
        });
    }*/

    public static void startPushService() {
        SharedPreferences preferences = applicationContext.getSharedPreferences("Notifications", MODE_PRIVATE);

        if (preferences.getBoolean("pushService", true)) {
            applicationContext.startService(new Intent(applicationContext, NotificationsService.class));
        } else {
            stopPushService();
        }
    }

    public static void stopPushService() {
        applicationContext.stopService(new Intent(applicationContext, NotificationsService.class));

        PendingIntent pintent = PendingIntent.getService(applicationContext, 0, new Intent(applicationContext, NotificationsService.class), 0);
        AlarmManager alarm = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pintent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            LocaleController.getInstance().onDeviceConfigurationChange(newConfig);
            AndroidUtilities.checkDisplaySize(applicationContext, newConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initPlayServices() {
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (checkPlayServices()) {
                    if (UserConfig.pushString != null && UserConfig.pushString.length() != 0) {
                        FileLog.d("GCM regId = " + UserConfig.pushString);
                    } else {
                        FileLog.d("GCM Registration not found.");
                    }

                    //if (UserConfig.pushString == null || UserConfig.pushString.length() == 0) {
                    Intent intent = new Intent(applicationContext, GcmRegistrationIntentService.class);
                    startService(intent);
                    //} else {
                    //    FileLog.d("GCM regId = " + UserConfig.pushString);
                    //}
                } else {
                    FileLog.d("No valid Google Play Services APK found.");
                }
            }
        }, 1000);
    }

    /*private void initPlayServices() {
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (checkPlayServices()) {
                    if (UserConfig.pushString != null && UserConfig.pushString.length() != 0) {
                        FileLog.d("GCM regId = " + UserConfig.pushString);
                    } else {
                        FileLog.d("GCM Registration not found.");
                    }
                    try {
                        if (!FirebaseApp.getApps(ApplicationLoader.applicationContext).isEmpty()) {
                            String token = FirebaseInstanceId.getInstance().getToken();
                            if (token != null) {
                                sendRegIdToBackend(token);
                            }
                        }
                    } catch (Throwable e) {
                        FileLog.e(e);
                    }
                } else {
                    FileLog.d("No valid Google Play Services APK found.");
                }
            }
        }, 2000);
    }*/

    private boolean checkPlayServices() {
        try {
            int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
            return resultCode == ConnectionResult.SUCCESS;
        } catch (Exception e) {
            FileLog.e(e);
        }
        return true;

        /*if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("tmessages", "This device is not supported.");
            }
            return false;
        }
        return true;*/
    }
}
