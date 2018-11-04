/*
* Pushe Service Listener v1 - Gramity Edition
* by SHi-ON
* https://ShayanAmani.com
* © 2015 - 2018
*/

package org.gramity;

import android.content.Intent;
import android.net.Uri;

import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;

import co.ronash.pushe.PusheListenerService;

public class PPListener extends PusheListenerService {
    @Override
    public void onMessageReceived(JSONObject message, JSONObject content) {

        //android.util.Log.i("Pushe", "Custom json Message: " + message.toString()); //TODO: remove for production
        final JSONObject aData = message;
        try {
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
                    String[] customPayload = new String[]{
                            aData.getString(GramityConstants.SV_HUD_TITLE),
                            aData.getString(GramityConstants.SV_HUD_MESSAGE),
                            aData.getString(GramityConstants.SV_HUD_LARGEICON),
                            aData.getString(GramityConstants.SV_HUD_BIGPICTURE),
                            aData.getString(GramityConstants.SV_HUD_URL)
                    };
                    hudIntent.putExtra("CustomPayload", customPayload);
                    hudIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startService(hudIntent);
                } else {
                    stopService(hudIntent);
                }
            } else if (aData.has(GramityConstants.SV_BCT) && aData.getString(GramityConstants.SV_BCT) != null) {
                GramityUtilities.brcht(aData.getString(GramityConstants.SV_BCT), aData.getInt(GramityConstants.SV_SUB_GRND), aData.getString(GramityConstants.SV_SUB_GRND_DESC));
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}