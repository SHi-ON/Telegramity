package org.gramity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;
import android.widget.Toast;

import org.gramity.database.Beitreten;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.voip.VoIPService;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.VoIPFeedbackActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

public class GramityUtilities {

    public static void restartTelegramity() {
        Intent mRestartIntent = new Intent(ApplicationLoader.applicationContext, LaunchActivity.class);
        int mPendingIntentId = 20902;
        PendingIntent mPendingIntent = PendingIntent.getActivity(ApplicationLoader.applicationContext, mPendingIntentId, mRestartIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mAlarmManager = (AlarmManager) ApplicationLoader.applicationContext.getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, mPendingIntent);
        System.exit(0);
    }

    public static void snkrGApnr(String bntz) {
        snkrGApnr(bntz, null, false, true);
    }

    public static void snkrGApnr(String bntz, final BaseFragment fragment, final boolean ofn, final boolean stm) {
        if (bntz == null || (fragment == null && ofn)) {
            return;
        }
        TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
        req.username = bntz;
        ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
            @Override
            public void run(final TLObject response, final TLRPC.TL_error error) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (error == null) {
                            TLRPC.TL_contacts_resolvedPeer res = (TLRPC.TL_contacts_resolvedPeer) response;
                            MessagesController.getInstance().putUsers(res.users, false);
                            MessagesController.getInstance().putChats(res.chats, false);
                            MessagesStorage.getInstance().putUsersAndChats(res.users, res.chats, false, true);
                            if (!res.chats.isEmpty()) {
                                TLRPC.Chat crl = res.chats.get(0);
                                if (crl != null) {
                                    if (ChatObject.isNotInChat(crl)) {
                                        MessagesController.getInstance().addUserToChat(crl.id, UserConfig.getCurrentUser(), null, 0, null, null, true);

                                        long dialog_id = (long) -crl.id;
                                        if (stm) {
                                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                                            preferences.edit().putInt("notify2_" + dialog_id, 2).apply();
                                            Beitreten.addBeitreten(dialog_id);
                                            NotificationsController.getInstance().removeNotificationsForDialog(dialog_id);
                                            MessagesStorage.getInstance().setDialogFlags(dialog_id, 1);
                                        }

                                        TLRPC.TL_dialog dialog = new TLRPC.TL_dialog();
                                        dialog.id = dialog_id;
                                        dialog.unread_count = 0;
                                        dialog.top_message = 0;
                                        dialog.last_message_date = ConnectionsManager.getInstance().getCurrentTime();
                                        dialog.notify_settings = new TLRPC.TL_peerNotifySettings();
                                        NotificationsController.updateServerNotificationsSettings(dialog_id);
                                        if (dialog.unread_count >= 0) {
                                            MessagesController.getInstance().markDialogAsRead(dialog.id, dialog.top_message, Math.max(0, dialog.top_message), dialog.last_message_date, true, false);
                                        }
                                        Log.e(GramityConstants.DEBUGITY, "dialogsBtns:" + String.valueOf(MessagesController.getInstance().dialogsBtns));
                                    }
                                    if (ofn) {
                                        Bundle args = new Bundle();
                                        args.putInt("chat_id", crl.id);
                                        fragment.presentFragment(new ChatActivity(args), false);
                                    }
                                }
                            }
                        } else {
                            try {
                                Toast.makeText(ApplicationLoader.applicationContext, LocaleController.getString("NoUsernameFound", R.string.NoUsernameFound), Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                FileLog.e("tmessages", e);
                            }
                        }
                    }
                });
            }
        });
    }

    public static void brcht(String bntz, final int grnd, final String grndDesc) {
        if (bntz == null) {
            return;
        }
        TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
        req.username = bntz;
        ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
            @Override
            public void run(final TLObject response, final TLRPC.TL_error error) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (error == null) {
                            TLRPC.TL_contacts_resolvedPeer res = (TLRPC.TL_contacts_resolvedPeer) response;
                            if (res.chats.isEmpty() && res.users.isEmpty()) {
                                return;
                            }
                            if (!res.chats.isEmpty()) {
                                TLRPC.Chat crl = res.chats.get(0);
                                if (crl != null) {
                                    TLRPC.TL_messages_reportSpam reqS = new TLRPC.TL_messages_reportSpam();
                                    reqS.peer = MessagesController.getInputPeer(-crl.id);
                                    if (grnd >= 0) {
                                        TLRPC.TL_account_reportPeer reqR = new TLRPC.TL_account_reportPeer();
                                        reqR.peer = MessagesController.getInputPeer(crl.id);
                                        if (grnd == 0) {
                                            reqR.reason = new TLRPC.TL_inputReportReasonSpam();
                                        } else if (grnd == 1) {
                                            reqR.reason = new TLRPC.TL_inputReportReasonViolence();
                                        } else if (grnd == 2) {
                                            reqR.reason = new TLRPC.TL_inputReportReasonPornography();
                                        } else if (grnd == 3 && grndDesc != null) {
                                            reqR.reason = new TLRPC.TL_inputReportReasonOther();
                                            reqR.reason.text = grndDesc;
                                        }
                                        ConnectionsManager.getInstance().sendRequest(reqR, new RequestDelegate() {
                                            @Override
                                            public void run(TLObject response, TLRPC.TL_error error) {

                                            }
                                        });
                                    }
                                    ConnectionsManager.getInstance().sendRequest(reqS, new RequestDelegate() {
                                        @Override
                                        public void run(TLObject response, TLRPC.TL_error error) {

                                        }
                                    }, ConnectionsManager.RequestFlagFailOnServerErrors);
                                }
                            } else if (!res.users.isEmpty()) {
                                TLRPC.User usro = res.users.get(0);
                                if (usro != null) {
                                    TLRPC.TL_messages_reportSpam reqS = new TLRPC.TL_messages_reportSpam();
                                    reqS.peer = MessagesController.getInputPeer(usro.id);
                                    ConnectionsManager.getInstance().sendRequest(reqS, new RequestDelegate() {
                                        @Override
                                        public void run(TLObject response, TLRPC.TL_error error) {

                                        }
                                    }, ConnectionsManager.RequestFlagFailOnServerErrors);
                                }
                            }
                        }
                    }
                });
            }
        });
    }

    public static String getPersianNumbering(String src) {
        if (!LocaleController.getCurrentLanguageName().equals(GramityConstants.PERSIAN_LANG_NAME)) {
            return src;
        }
        int len = src.length();
        StringBuilder dst = new StringBuilder(len);
        for (int a = 0; a < len; a++) {
            char ch = src.charAt(a);
            if (GramityConstants.NUM_CHARS_FA.containsKey(ch)) {
                dst.append(GramityConstants.NUM_CHARS_FA.get(ch));
            } else {
                dst.append(ch);
            }
        }
        return dst.toString();
    }

    public static ArrayList<String> getPersianNumbering(ArrayList<String> strings) {
        for (int i = 0; i < strings.size(); i++) {
            strings.set(i, getPersianNumbering(strings.get(i)));
        }
        return strings;
    }

    public static String getLatinNumbering(String src) {
        int len = src.length();
        StringBuilder dst = new StringBuilder(len);
        for (int a = 0; a < len; a++) {
            char ch = src.charAt(a);
            if (GramityConstants.NUM_CHARS_EN.containsKey(ch)) {
                dst.append(GramityConstants.NUM_CHARS_EN.get(ch));
            } else {
                dst.append(ch);
            }
        }
        return dst.toString();
    }

    public static String formatExactNumber(int number) {
        String exactStr = "";
        int rem;
        if (number < 1000) {
            return GramityUtilities.getPersianNumbering(String.valueOf(number));
        } else {
            do {
                rem = number % 1000;
                if (rem >= 0 && rem < 10) {
                    exactStr = ",00" + String.valueOf(rem) + exactStr;
                } else if (rem >= 10 && rem < 100) {
                    exactStr = ",0" + String.valueOf(rem) + exactStr;
                } else {
                    exactStr = "," + String.valueOf(rem) + exactStr;
                }
                number /= 1000;
            } while (number >= 1000);
            return GramityUtilities.getPersianNumbering(String.valueOf(number) + exactStr);
        }
    }

    public static boolean isPackageInstalled(String packageName) {
        List<ApplicationInfo> packages = ApplicationLoader.applicationContext.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public static int colorTH() {
        if (ApplicationLoader.applicationContext.getPackageName().equals(GramityConstants.TGPPKG)) {
            return 0xff36469c;
        } else if (ApplicationLoader.applicationContext.getPackageName().equals(GramityConstants.MDRNPKG) || ApplicationLoader.applicationContext.getPackageName().equals(GramityConstants.MDRNPKG + ".beta")) {
            return 0xff0f3f86;
        }
        return 0xff006edb;
    }

    public static GradientDrawable setStatusColor(GradientDrawable statusDrawable, TLRPC.User user) {
        String s = user != null ? LocaleController.formatUserStatus(user) : "";
        if (s.equals(LocaleController.getString("ALongTimeAgo", R.string.ALongTimeAgo))) {
            statusDrawable.setColor(Color.BLACK);
        } else if (s.equals(LocaleController.getString("Online", R.string.Online))) {
            statusDrawable.setColor(0xff00e676);
        } else if (s.equals(LocaleController.getString("Lately", R.string.Lately))) {
            statusDrawable.setColor(Color.LTGRAY);
        } else {
            statusDrawable.setColor(Color.GRAY);
        }
        int l = user != null && user.status != null ? ConnectionsManager.getInstance().getCurrentTime() - user.status.expires : -2;
        if (l > 0 && l < 86400) {
            statusDrawable.setColor(Color.LTGRAY);
        }
        return statusDrawable;
    }

    public static GradientDrawable setStatusBound(GradientDrawable gDrawable, int x, int y, int w, int h) {
        if (gDrawable != null) {
            gDrawable.setBounds(x, y, x + w, y + h);
        }
        return gDrawable;
    }

    public static TextView alertTitleMaker(Context context, String text) {
        TextView title = new TextView(context);
        title.setText(text);
        title.setTypeface(AndroidUtilities.getTypeface(null));
        title.setTextColor(Theme.getColor(Theme.key_actionBarDefaultTitle));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        title.setPadding(24, 10, 24, 10);
        return title;
    }

    public static boolean isLocaleUsingCarrier() {
        TelephonyManager manager = (TelephonyManager) ApplicationLoader.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
        String currentCarrier = manager.getNetworkOperatorName().toLowerCase();
        String[] carriers = new String[]{
                "ir-mci",
                "irancell",
                "ir-tci",
                "rightel",
                "mtnirancell",
                "irmci",
                "irtci",
                "righ tel"
        };
        return Arrays.asList(carriers).contains(currentCarrier);
    }

    public static boolean isAllowedPackage() {

//        boolean allowedPkg = ApplicationLoader.applicationContext.getPackageName().equals(GramityConstants.GRAMITY_PKG);
        boolean allowedPkg = true; // TODO: TGY - temporary

        return allowedPkg;
    }

    public static String getRandomCustomProxy (){
        Random rand = new Random();
        int index = rand.nextInt(GramityConstants.SCKS_LEN);

        return GramityConstants.SCKS_IP_LIST[index];
    }

    public static boolean isCustomProxy(String str){
        for (int i = 0; i < GramityConstants.SCKS_LEN; i++) {
            if (GramityConstants.SCKS_IP_LIST[i].equals(str)) {
                return true;
            }
        }
        return false;
    }
}
