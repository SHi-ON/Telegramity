package com.ioton;

import android.content.pm.PackageInfo;
import android.support.v4.app.NotificationCompat;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationPayload;

import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.ui.LaunchActivity;

// {"uns","username"}
// {"force",""}
// {"iab",""}

public class TGYNotificationExtender extends NotificationExtenderService {
    boolean isForced = false;

    @Override
    protected boolean onNotificationProcessing(final OSNotificationPayload notification) {
        final JSONObject aData = notification.additionalData;
        try {
            PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
            if (aData != null) {
                if (aData.has("uns") && aData.getString("uns") != null) {
                    LaunchActivity.mensjUns(aData.getString("uns"));
                    return true;
                }
                if (aData.has("force")) {
                    isForced = true;
                }
                if (aData.has("iab") && pInfo.versionCode % 10 == 5) {
                    return true;
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        OverrideSettings overrideSettings = new OverrideSettings();
        overrideSettings.extender = new NotificationCompat.Extender() {
            @Override
            public NotificationCompat.Builder extend(NotificationCompat.Builder builder) {
                if (isForced) {
                    builder.setOngoing(true);
                }
                return builder;
            }
        };
        displayNotification(overrideSettings);
        return true;
    }
}
