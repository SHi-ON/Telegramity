package org.gramity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class AnsweringMachine {

    private static HashMap<Long, Long> list = new HashMap<>();

    public static void ProcessMsgs(ArrayList<MessageObject> listOfDialogs) {
        for (int i = 0; i < listOfDialogs.size(); i++) {
            final MessageObject m = listOfDialogs.get(i);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ProcessMsg(m);
                }
            }, i * 1000);
        }
    }

    private static boolean ProcessMsg(MessageObject messageObject) {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Context.MODE_PRIVATE);
        boolean answering = preferences.getBoolean(GramityConstants.PREF_ANSWERING_MACHINE, false);
        String answeringMessage = preferences.getString(GramityConstants.PREF_ANSWERING_MACHINE_MESSAGE, LocaleController.getString("AnsweringMachineDefaultMessage", R.string.AnsweringMachineDefaultMessage));
        if (answering && answeringMessage.length() > 0) {
            long userId = messageObject.getDialogId();
            TLRPC.User user = MessagesController.getInstance().getUser((int) userId);
            if (userId > 0 && user != null && !user.bot) {
                if (isOk(userId)) {
                    SendText(answeringMessage, userId, messageObject);
                    add(userId);
                    return false;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    private static void SendText(String msg, long userId, MessageObject msgForReplay) {
        SendMessagesHelper.getInstance().sendMessage(msg, userId, null, null, true, null, null, null);
        MessagesController.getInstance().markMessageContentAsRead(msgForReplay);
    }

    private static boolean isOk(Long userId) {
        if (list.size() == 0) return true;
        if (list.containsKey(userId)) {
            if (list.get(userId) < new Date().getTime()) {
                list.remove(userId);
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public static void add(long userId) {
        list.remove(userId);
        list.put(userId, new Date().getTime() + (20 * 1000));
    }
}
