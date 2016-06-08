package com.ioton;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
//    public static final String TGYLNK = "BL6mIDvhlAL5kCBf8yOwRg";
    public static final String DEFAULT_FONT_PATH = "IRANSansMobile";
    public static final String DEBUGITY = "SHi_ON";
    public static int ABBG_COLOR = 0xffef3f3e;
    public static int DH_COLOR = 0xff2196f3;
    public static int PBG_COLOR = 0xff9c27b0;

    public static void restartTelegramity() {
        Intent mRestartIntent = new Intent(ApplicationLoader.applicationContext, LaunchActivity.class);
        int mPendingIntentId = 20902;
        PendingIntent mPendingIntent = PendingIntent.getActivity(ApplicationLoader.applicationContext, mPendingIntentId, mRestartIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mAlarmManager = (AlarmManager) ApplicationLoader.applicationContext.getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, mPendingIntent);
        System.exit(0);
    }

    public static void emisioAbridrYUnise(String nombrDeUsuro, final BaseFragment fragment, final int type, final boolean abrr, final boolean unse) {
        if (nombrDeUsuro == null || fragment == null) {
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(fragment.getParentActivity());
        progressDialog.setMessage(LocaleController.getString("Loading", R.string.Loading));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);

        TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
        req.username = nombrDeUsuro;
        final int reqId = ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
            @Override
            public void run(final TLObject response, final TLRPC.TL_error error) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            progressDialog.dismiss();
                        } catch (Exception e) {
                            FileLog.e("tmessages", e);
                        }
                        fragment.setVisibleDialog(null);
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
                                            MessagesController.getInstance().addUserToChat(chrla.id, UserConfig.getCurrentUser(), null, 0, null, fragment);
                                        }
                                    }
                                }
                                if (abrr) {
                                    if (type == 0) {
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
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, LocaleController.getString("Cancel", R.string.Cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ConnectionsManager.getInstance().cancelRequest(reqId, true);
                try {
                    dialog.dismiss();
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }
                if (fragment != null) {
                    fragment.setVisibleDialog(null);
                }
            }
        });
        fragment.setVisibleDialog(progressDialog);
        progressDialog.show();
    }

}
