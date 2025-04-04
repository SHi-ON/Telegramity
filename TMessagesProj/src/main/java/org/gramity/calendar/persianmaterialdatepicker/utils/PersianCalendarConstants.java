package org.gramity.calendar.persianmaterialdatepicker.utils;

public class PersianCalendarConstants {

    // 00:00:00 UTC (Gregorian) Julian day 0,
    // 0 milliseconds since 1970-01-01
    public static final long MILLIS_JULIAN_EPOCH = -210866803200000L;
    // Milliseconds of a day calculated by 24L(hours) * 60L(minutes) *
    // 60L(seconds) * 1000L(mili);
    public static final long MILLIS_OF_A_DAY = 86400000L;

    /**
     * The JDN of 1 Farvardin 1; Equivalent to March 19, 622 A.D.
     */
    public static final long PERSIAN_EPOCH = 1948321;

    public static final String DAY = "day";
    public static final String IS_OUT_OF_RANGE = "is out of range!";
    public static final String NOT_IMPLEMENTED_YET = "not implemented yet!";
    public static final String MONTH = "month";
    public static final String YEAR_0_IS_INVALID = "Year 0 is invalid!";

    public static final String[] JALALI_MONTH_NAMES_FA = {
            "\u0641\u0631\u0648\u0631\u062f\u06cc\u0646", // Farvardin
            "\u0627\u0631\u062f\u06cc\u0628\u0647\u0634\u062a", // Ordibehesht
            "\u062e\u0631\u062f\u0627\u062f", // Khordad
            "\u062a\u06cc\u0631", // Tir
            "\u0645\u0631\u062f\u0627\u062f", // Mordad
            "\u0634\u0647\u0631\u06cc\u0648\u0631", // Shahrivar
            "\u0645\u0647\u0631", // Mehr
            "\u0622\u0628\u0627\u0646", // Aban
            "\u0622\u0630\u0631", // Azar
            "\u062f\u06cc", // Dey
            "\u0628\u0647\u0645\u0646", // Bahman
            "\u0627\u0633\u0641\u0646\u062f" // Esfand
    };

    public static final String[] JALALI_MONTH_NAMES_EN = new String[]{
            "Farvardin",
            "Ordibehesht",
            "Khordad",
            "Tir",
            "Mordad",
            "Shahrivar",
            "Mehr",
            "Aban",
            "Azar",
            "Dey",
            "Bahman",
            "Esfand"
    };


    public static final String[] persianWeekDays = {
            "\u0634\u0646\u0628\u0647", // Shanbeh
            "\u06cc\u06a9\u200c\u0634\u0646\u0628\u0647", // Yekshanbeh
            "\u062f\u0648\u0634\u0646\u0628\u0647", // Doshanbeh
            "\u0633\u0647\u200c\u0634\u0646\u0628\u0647", // Sehshanbeh
            "\u0686\u0647\u0627\u0631\u0634\u0646\u0628\u0647", // Chaharshanbeh
            "\u067e\u0646\u062c\u200c\u0634\u0646\u0628\u0647", // Panjshanbeh
            "\u062c\u0645\u0639\u0647" // Jom'e
    };

}
