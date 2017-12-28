package org.gramity.database;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;

public class Beitreten {

    long id;
    long chat_id;

    public Beitreten() {

    }

    public Beitreten(long id, long chat_id) {
        this.id = id;
        this.chat_id = chat_id;
    }

    public Beitreten(long chat_id) {
        this.chat_id = chat_id;
    }

    public long getChatID() {
        return this.chat_id;
    }

    public long getID() {
        return this.id;
    }

    public void setChatID(long chat_id) {
        this.chat_id = chat_id;
    }

    public void setID(long id) {
        this.id = id;
    }

    public static void addBeitreten(Long id) {
        Beitreten beitreten = new Beitreten(id);
        ApplicationLoader.databaseHandler.addBeitreten(beitreten);
    }

    public static void deleteBeitreten(Long id) {
        ApplicationLoader.databaseHandler.deleteBeitreten(id);
    }

    public static boolean isBeitreten(Long id) {
        if (id == null) {
            return false;
        }
        try {
            Beitreten beitreten = ApplicationLoader.databaseHandler.getBeitretenByChatId(id);
            if (beitreten == null) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
            return false;
        }
    }
}

