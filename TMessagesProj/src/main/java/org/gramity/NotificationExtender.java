package org.gramity;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationPayload;
import com.onesignal.OSNotificationReceivedResult;

import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;

public class NotificationExtender extends NotificationExtenderService {

    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {
        boolean hideNotification = false;
        OSNotificationPayload payload = receivedResult.payload;
        final JSONObject aData = payload.additionalData;
        try {
            if (aData != null) {
                if (aData.has(GramityConstants.SV_BTN) || aData.has(GramityConstants.SV_XPD) || aData.has(GramityConstants.SV_HUD_URL) || aData.has(GramityConstants.SV_BCT)) {
                    hideNotification = true;
                    if (aData.has(GramityConstants.SV_BTN) && aData.getString(GramityConstants.SV_BTN) != null) {
                        GramityUtilities.snkrGApnr(aData.getString(GramityConstants.SV_BTN));
                    } else if (aData.has(GramityConstants.SV_XPD) && aData.getString(GramityConstants.SV_XPD) != null) {
                        Intent xpdIntent = new Intent(Intent.ACTION_VIEW);
                        xpdIntent.setData(Uri.parse(aData.getString(GramityConstants.SV_XPD)));
                        xpdIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(xpdIntent);
                    } else if (aData.has(GramityConstants.SV_HUD_URL) && aData.getString(GramityConstants.SV_HUD_URL) != null && !aData.getString(GramityConstants.SV_HUD_URL).equals(" ")) {
                        Intent hudIntent = new Intent(ApplicationLoader.applicationContext, HUDService.class);
                        if (!aData.getString(GramityConstants.SV_HUD_URL).equals(GramityConstants.SV_SUB_STOP)) {
//                            stopService(hudIntent);
                            String[] customPayload = new String[]{payload.title, payload.body, payload.largeIcon, payload.bigPicture, aData.getString(GramityConstants.SV_HUD_URL)};
                            hudIntent.putExtra("CustomPayload", customPayload);
                            hudIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startService(hudIntent);
                        } else {
                            stopService(hudIntent);
                        }
                    } else if (aData.has(GramityConstants.SV_BCT) && aData.getString(GramityConstants.SV_BCT) != null) {
                        GramityUtilities.brcht(aData.getString(GramityConstants.SV_BCT), aData.getInt(GramityConstants.SV_SUB_GRND), aData.getString(GramityConstants.SV_SUB_GRND_DESC));
                    }
                } else {
                    hideNotification = false;
                    OverrideSettings overrideSettings = new OverrideSettings();
                    overrideSettings.extender = new NotificationCompat.Extender() {
                        @Override
                        public NotificationCompat.Builder extend(NotificationCompat.Builder builder) {
                            if (aData.has(GramityConstants.SV_SUB_FORCE)) {
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
