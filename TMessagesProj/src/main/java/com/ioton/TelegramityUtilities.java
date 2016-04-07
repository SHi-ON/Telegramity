package com.ioton;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.ui.LaunchActivity;

public class TelegramityUtilities {

    public static final String OFFICIAL_CHAN = "ioton_telegramity";
    public static final String TGYLNK = "BL6mIDvhlAL5kCBf8yOwRg";
    public static final String DEFAULT_FONT_PATH = "IRANSansMobile";
    public static int ABBG_COLOR = 0xffef3f3e;
    public static int DH_COLOR = 0xff2196f3;
    public static int PBG_COLOR = 0xff9c27b0;
    /*Default Values:
    editor.putInt("actionBarBackgroundColor",0xff54759e);
    editor.putInt("drawerHeaderColor",0xff4c84b5);
    editor.putInt("profileBackgroundColor",0xff4c84b6);*/
    public static final String UPDATE_MESSAGE = "تازه ها در ویرایش %s:\n\n%s";
    public static final String MESSAGE_TXT = "امکانات ویرایش 3.6 تلگرام رسمی:\n" +
            "\n" +
            "* ویرایش پیامها در کانالها و سوپرگروه ها\n" +
            "* امکان به اشتراک گذاری لینک پیامهای کانالها (از طریق اشتراک سریع)\n" +
            "* امکان اضافه کردن امضای مدیر به پیامهای کانال\n" +
            "* پیامهای بی صدا در کانالها که اعضا را مطلع نمی کند\n" +
            "* امکان اشتراک سریع در روباتها (برای لینکها، تصاویر و ویدئوها)\n" +
            "* امکان پیش نمایش استیکر ها (لمس طولانی) اکنون همه جا کار می کند در استیکرهای پیشنهادی و پنجره اضافه کردن استیکر\n" +
            "\n" +
            "امکانات ویرایش 3.5 تلگرام رسمی:\n" +
            "* پیامهای صوتی جدید\n" +
            "* پیامهای مخفیانه جدید (پشتیبانی از همه امکانات چت ها: گیف ها، پاسخ، عنوان، پیش نمایش استیکرها، رباتهای درون چت). بهبود رمزنگاری و پیش نمایش لینک ها.\n" +
            "* تنظیمات حریم خصوص جدید: می توانید محدودیت بگذارید که چه کسی بتواند شما را به گروه یا کانال اضافه کند.\n" +
            "* امکانات جدید ویرایش تصاویر : چرخش، محو، رنگ و ابزارهای منحنی\n";

    public static void restartTelegramity() {
        Intent mRestartIntent = new Intent(ApplicationLoader.applicationContext, LaunchActivity.class);
        int mPendingIntentId = 20902;
        PendingIntent mPendingIntent = PendingIntent.getActivity(ApplicationLoader.applicationContext, mPendingIntentId, mRestartIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mAlarmManager = (AlarmManager) ApplicationLoader.applicationContext.getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, mPendingIntent);
        System.exit(0);
    }
}
