package com.ioton;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.ProfileActivity;

public class TelegramityUtilities {

    private static TLRPC.Chat chrla = null;

    public static final String OFFICIAL_CHAN = "ioton_telegramity";
    public static final String DEFAULT_FONT_PATH = "IRANSansMobile";
    public static final String DEBUGITY = "SHi_ON";
    public static final String TGYPACKAGENAME = "org.telegram.engmariaamani.messenger";
    public static final String PTGPACKAGENAME = "org.telegram.engmariaamani.parsi";

    public static int colorABBG() {
        try {
            PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
            if (pInfo.versionCode % 10 == 5) {
                return 0xff3f51b5;
            }
        } catch (PackageManager.NameNotFoundException e) {
            FileLog.e("tmessages", e);
        }
        return 0xffef3f3e;
    }

    public static int colorTH() {
        try {
            PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
            if (pInfo.versionCode % 10 == 5) {
                return 0xff36469c;
            }
        } catch (PackageManager.NameNotFoundException e) {
            FileLog.e("tmessages", e);
        }
        return 0xff006edb;
    }

    public static int colorDH() {
        try {
            PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
            if (pInfo.versionCode % 10 == 5) {
                return 0xfff44336;
            }
        } catch (PackageManager.NameNotFoundException e) {
            FileLog.e("tmessages", e);
        }
        return 0xff2196f3;
    }

    public static int colorPBG() {
        try {
            PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
            if (pInfo.versionCode % 10 == 5) {
                return 0xff008fa1;
            }
        } catch (PackageManager.NameNotFoundException e) {
            FileLog.e("tmessages", e);
        }
        return 0xff9c27b0;
    }

    public static void restartTelegramity() {
        Intent mRestartIntent = new Intent(ApplicationLoader.applicationContext, LaunchActivity.class);
        int mPendingIntentId = 20902;
        PendingIntent mPendingIntent = PendingIntent.getActivity(ApplicationLoader.applicationContext, mPendingIntentId, mRestartIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mAlarmManager = (AlarmManager) ApplicationLoader.applicationContext.getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, mPendingIntent);
        System.exit(0);
    }

    public static void emisioAbridrYUnise(String nombrDeUsuro, final BaseFragment fragment, final int tip, final boolean abrr, final boolean unse) {
        if (nombrDeUsuro == null || fragment == null) {
            return;
        }
        TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
        req.username = nombrDeUsuro;
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
                                chrla = res.chats.get(0);
                                Bundle args = new Bundle();
                                if (chrla != null) {
                                    args.putInt("chat_id", chrla.id);
                                    if (unse) {
                                        if (ChatObject.isNotInChat(chrla)) {
                                            MessagesController.getInstance().addUserToChat(chrla.id, UserConfig.getCurrentUser(), null, 0, null, fragment, unse);
                                        }
                                    }
                                }
                                if (abrr) {
                                    if (tip == 0) {
                                        fragment.presentFragment(new ProfileActivity(args));
                                    } else {
                                        fragment.presentFragment(new ChatActivity(args), false);
                                    }
                                }
                            }
                        } else {
                            if (fragment != null && fragment.getParentActivity() != null) {
                                try {
                                    Toast.makeText(fragment.getParentActivity(), LocaleController.getString("NoUsernameFound", R.string.NoUsernameFound), Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    FileLog.e("tmessages", e);
                                }
                            }
                        }
                    }
                });
            }
        });
    }

}
