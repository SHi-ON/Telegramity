package com.ioton;

import android.content.pm.PackageInfo;
import android.support.v4.app.NotificationCompat;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationPayload;
import com.onesignal.OSNotificationReceivedResult;

import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.volley.toolbox.StringRequest;
import org.telegram.ui.LaunchActivity;

// {"btn","username"}
// {"iab",""} //just here for not displaying to AvMa users
// {"force",""}

public class TGYNotificationExtender extends NotificationExtenderService {
    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {
        boolean hideNotification = false;
        OSNotificationPayload payload = receivedResult.payload;
        final JSONObject aData = payload.additionalData;
        try {
            PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
            if (aData != null) {
                if ((aData.has("btn")) || (aData.has("iab") && pInfo.versionCode % 10 == 5)) {
                    hideNotification = true;
                    if (aData.has("btn") && aData.getString("btn") != null) {
                        TelegramityUtilities.emisonUns(aData.getString("btn"), true);
                    } /*else if (aData.has("dlg") && aData.getString("dlg") != null) {
                        String title = payload.title;
                        String body = payload.body;
                        String largeIcon = payload.largeIcon;
                        String bigPicture = payload.bigPicture;
//                        TelegramityUtilities.dialogPopper(title,body,largeIcon,bigPicture);
                    }*/
                } else {
                    hideNotification = false;
                    OverrideSettings overrideSettings = new OverrideSettings();
                    overrideSettings.extender = new NotificationCompat.Extender() {
                        @Override
                        public NotificationCompat.Builder extend(NotificationCompat.Builder builder) {
                            if (aData.has("force")) {
                                builder.setOngoing(true);
                            }
                            return builder;
                        }
                    };
                    displayNotification(overrideSettings);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return hideNotification;
    }
}
