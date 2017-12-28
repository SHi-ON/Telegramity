package org.gramity.calendar.dates;

import org.gramity.calendar.DateConverter;
import org.gramity.calendar.exceptions.DayOutOfRangeException;
import org.gramity.calendar.exceptions.MonthOutOfRangeException;
import org.gramity.calendar.exceptions.YearOutOfRangeException;
import org.gramity.calendar.persianmaterialdatepicker.utils.PersianCalendarConstants;
import org.telegram.messenger.LocaleController;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class PersianDate extends AbstractDate {
    private int year;
    private int month;
    private int day;

    public PersianDate(int year, int month, int day) {
        setYear(year);
        // Initialize day, so that we get no exceptions when setting month
        this.day = 1;
        setMonth(month);
        setDayOfMonth(day);
    }

    public PersianDate clone() {
        return new PersianDate(getYear(), getMonth(), getDayOfMonth());
    }

    public int getDayOfMonth() {
        return day;
    }

    public void setDayOfMonth(int day) {
        if (day < 1)
            throw new DayOutOfRangeException(
                    PersianCalendarConstants.DAY + " " + day + " " + PersianCalendarConstants.IS_OUT_OF_RANGE);

        if (month <= 6 && day > 31)
            throw new DayOutOfRangeException(
                    PersianCalendarConstants.DAY + " " + day + " " + PersianCalendarConstants.IS_OUT_OF_RANGE);

        if (month > 6 && month <= 12 && day > 30)
            throw new DayOutOfRangeException(
                    PersianCalendarConstants.DAY + " " + day + " " + PersianCalendarConstants.IS_OUT_OF_RANGE);

        if (isLeapYear() && month == 12 && day > 30)
            throw new DayOutOfRangeException(
                    PersianCalendarConstants.DAY + " " + day + " " + PersianCalendarConstants.IS_OUT_OF_RANGE);

        if ((!isLeapYear()) && month == 12 && day > 29)
            throw new DayOutOfRangeException(
                    PersianCalendarConstants.DAY + " " + day + " " + PersianCalendarConstants.IS_OUT_OF_RANGE);

        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        if (month < 1 || month > 12)
            throw new MonthOutOfRangeException(
                    PersianCalendarConstants.MONTH + " " + month + " " + PersianCalendarConstants.IS_OUT_OF_RANGE);

        // Set the day again, so that exceptions are thrown if the
        // day is out of range
        setDayOfMonth(day);

        this.month = month;
    }

    public int getWeekOfYear() {
        throw new RuntimeException(PersianCalendarConstants.NOT_IMPLEMENTED_YET);
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        if (year == 0)
            throw new YearOutOfRangeException(PersianCalendarConstants.YEAR_0_IS_INVALID);

        this.year = year;
    }

    public void rollDay(int amount, boolean up) {
        throw new RuntimeException(PersianCalendarConstants.NOT_IMPLEMENTED_YET);
    }

    public void rollMonth(int amount, boolean up) {
        throw new RuntimeException(PersianCalendarConstants.NOT_IMPLEMENTED_YET);
    }

    public void rollYear(int amount, boolean up) {
        throw new RuntimeException(PersianCalendarConstants.NOT_IMPLEMENTED_YET);
    }

    public String getEvent() {
        throw new RuntimeException(PersianCalendarConstants.NOT_IMPLEMENTED_YET);
    }

    public int getDayOfWeek() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    public int getDayOfYear() {
        throw new RuntimeException(PersianCalendarConstants.NOT_IMPLEMENTED_YET);
    }

    public int getWeekOfMonth() {
        throw new RuntimeException(PersianCalendarConstants.NOT_IMPLEMENTED_YET);
    }

    public boolean isLeapYear() {
        int y;
        if (year > 0)
            y = year - 474;
        else
            y = 473;
        return (((((y % 2820) + 474) + 38) * 682) % 2816) < 682;
    }

    public boolean equals(PersianDate persianDate) {
        return this.getDayOfMonth() == persianDate.getDayOfMonth()
                && this.getMonth() == persianDate.getMonth()
                && (this.getYear() == persianDate.getYear() || this.getYear() == -1);
    }

    public static String getJalaliMonthNameFa(int i) {
        return (i >= 1 || i <= 12) ? PersianCalendarConstants.JALALI_MONTH_NAMES_FA[i - 1] : null;
    }

    public static String getJalaliMonthNameEn(int i) {
        return (i >= 1 || i <= 12) ? PersianCalendarConstants.JALALI_MONTH_NAMES_EN[i - 1] : null;
    }

    public static PersianDate getPersianDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        if (LocaleController.getInstance().formatterMonth.getTimeZone().equals(TimeZone.getTimeZone("Asia/Tehran"))) {//TGY temp
            calendar.setTimeZone(TimeZone.getTimeZone("Asia/Tehran"));
        }
        calendar.setTime(date);
        return DateConverter.civilToPersian(new CivilDate(calendar));
    }
}
