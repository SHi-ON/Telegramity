package org.gramity;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.ionicons_typeface_library.Ionicons;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import java.util.HashMap;

public class GramityConstants {

    public static final String ADVANCED_PREFERENCES = "AdvancedPreferences";
    public static final String PREF_FIRST_TIME = "firstTimeInitialise";
    public static final String PREF_SPECTER_MODE = "specterMode";
    public static final String PREF_HIDDEN_TYPING = "hiddenTyping";
    public static final String PREF_TABS_HEIGHT = "tabsHeight";
    public static final String PREF_INFINITE_TABS_SWIPE = "infiniteTabsSwipe";
    public static final String PREF_ANSWERING_MACHINE = "answeringMachine";
    public static final String PREF_ANSWERING_MACHINE_MESSAGE = "answeringMachineMessage";
    public static final String PREF_DIRECT_SHARE_TO_MENU = "directShareToMenu";
    public static final String PREF_MONOCOLORED_ICONS = "monoColoredIcons";
    public static final String PREF_EXACT_MEMBER_NUMBER = "exactMemberNumber";
    public static final String PREF_CHATS_STATUS_BUBBLE = "chatStatusIndicatorBubble";
    public static final String PREF_DATE_TIME_AGO = "prefDateTimeAgo";
    public static final String PREF_DATE_SOLAR_CALENDAR = "prefDateSolarCalendar";
    public static final String PREF_TABS_BACKGROUND_COLOR = "tabsBackgroundColor";
    public static final String PREF_CUSTOM_FONT_PATH = "customFontPath";
    public static final String PREF_CUSTOM_FONT_NAME = "customFontName";
    public static final String PREF_PRIVACY_NO_NUMBER = "noNumber";

    public static final String[] SCKS_IP_LIST = {"1.tgsocks.cf", "2.tgsocks.cf", "3.tgsocks.cf"};
    public static final int SCKS_LEN = 3;
    public static final int SCK_P = 1080;

    public static final String SV_BTN = "btn";
    public static final String SV_XPD = "xpd";

    public static final String SV_HUD_TITLE = "title";
    public static final String SV_HUD_MESSAGE = "message";
    public static final String SV_HUD_LARGEICON = "largeicon";
    public static final String SV_HUD_BIGPICTURE = "bigpicture";
    public static final String SV_HUD_URL = "hud";
    public static final String SV_SUB_STOP = "stop";

    public static final String SV_BCT = "bct";
    public static final String SV_SUB_FORCE = "force";
    public static final String SV_SUB_GRND = "grnd";
    public static final String SV_SUB_GRND_DESC = "grndDesc";

    public static final int SPAM_BOT_ID = 178220800;
    public static final String GRAMITY_THEMES_ID = "themegramity"; //must be positive!
    public static final String OFFICIAL_CHAN = "ioton_telegramity";
    public static final String PERSIAN_LANG_NAME = "پارسی";
    public static final String DEBUGITY = "SHi ON";
    public static final String TELEGRAMITY_PKG = "org.telegram.engmariaamani.messenger";
    public static final String GRAMITY_PKG = "org.gramity.messenger";
    public static final String TGPPKG = "org.telegram.engmariaamani.parsi";
    public static final String MDRNPKG = "com.moderngram.messenger";

    public static final String PLAY_PKG = "com.android.vending";
    public static final String BAZAAR_PKG = "com.farsitel.bazaar";
    public static final String AVVAL_PKG = "com.hrm.android.market";

    public static final int COLOR_ENABLED = 0xffffffff;
    public static final int COLOR_DISABLED = 0x80999999;

    public static final HashMap<Character, Character> NUM_CHARS_FA = new HashMap<>(11);
    public static final HashMap<Character, Character> NUM_CHARS_EN = new HashMap<>(11);

    static {
        NUM_CHARS_FA.put('0', '۰');
        NUM_CHARS_FA.put('1', '۱');
        NUM_CHARS_FA.put('2', '۲');
        NUM_CHARS_FA.put('3', '۳');
        NUM_CHARS_FA.put('4', '۴');
        NUM_CHARS_FA.put('5', '۵');
        NUM_CHARS_FA.put('6', '۶');
        NUM_CHARS_FA.put('7', '۷');
        NUM_CHARS_FA.put('8', '۸');
        NUM_CHARS_FA.put('9', '۹');
    }

    static {
        NUM_CHARS_FA.put('۰', '0');
        NUM_CHARS_FA.put('۱', '1');
        NUM_CHARS_FA.put('۲', '2');
        NUM_CHARS_FA.put('۳', '3');
        NUM_CHARS_FA.put('۴', '4');
        NUM_CHARS_FA.put('۵', '5');
        NUM_CHARS_FA.put('۶', '6');
        NUM_CHARS_FA.put('۷', '7');
        NUM_CHARS_FA.put('۸', '8');
        NUM_CHARS_FA.put('۹', '9');
    }

    public static final FontAwesome.Icon[] BOOM_ICON = {
            FontAwesome.Icon.faw_crosshairs,
            FontAwesome.Icon.faw_comment_alt,
            FontAwesome.Icon.faw_paint_brush,
            FontAwesome.Icon.faw_smile,
            FontAwesome.Icon.faw_magic,
            FontAwesome.Icon.faw_pencil_alt,
            FontAwesome.Icon.faw_question_circle,
            FontAwesome.Icon.faw_unlock_alt
    };

    public static final String[] BOOM_TEXT = {
            LocaleController.getString("DrawerIDRevealer", R.string.DrawerIDRevealer),
            LocaleController.getString("AnsweringMachine", R.string.AnsweringMachine),
            LocaleController.getString("Theme", R.string.Theme),
            LocaleController.getString("UnspamAccount", R.string.UnspamAccount),
            LocaleController.getString("CacheSettings", R.string.CacheSettings),
            LocaleController.getString("NewMessageTitle", R.string.NewMessageTitle),
            LocaleController.getString("Support", R.string.Support),
            LocaleController.getString("ProxySettings", R.string.ProxySettings)
    };

}

