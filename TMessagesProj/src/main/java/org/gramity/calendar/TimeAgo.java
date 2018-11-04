package org.gramity.calendar;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeAgo {

    public static String FastGet(String Dates) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(Dates);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeAgo(convertedDate);
    }

    public static String timeAgo(Date date) {
        return timeAgo(date.getTime());
    }

    public static String timeAgo(long millis) {
        long diff = new Date().getTime() - millis;

        double seconds = Math.abs(diff) / 1000;
        double minutes = seconds / 60;
        double hours = minutes / 60;
        double days = hours / 24;
        double months = days / 24;
        double years = days / 12;

        if (seconds < 45) {
            return LocaleController.getString("timeAgoFewMoments", R.string.timeAgoFewMoments);
        } else if (seconds < 90) {
            return LocaleController.getString("timeAgoMinute", R.string.timeAgoMinute);
        } else if (minutes < 45) {
            return LocaleController.formatString("timeAgoMinutes", R.string.timeAgoMinutes, (int) Math.round(minutes));
        } else if (minutes < 90) {
            return LocaleController.getString("timeAgoHour", R.string.timeAgoHour);
        } else if (hours < 24) {
            return LocaleController.formatString("timeAgoHours", R.string.timeAgoHours, (int) Math.round(hours));
        } else if (hours < 42) {
            return LocaleController.getString("timeAgoDay", R.string.timeAgoDay);
        } else if (days < 30) {
            return LocaleController.formatString("timeAgoDays", R.string.timeAgoDays, (int) Math.round(days));
        } else if (days < 45) {
            return LocaleController.getString("timeAgoMonth", R.string.timeAgoMonth);
        } else if (months < 12) {
            return LocaleController.formatString("timeAgoMonths", R.string.timeAgoMonths, (int) Math.round(months));
        } else if (months < 18) {
            return LocaleController.getString("timeAgoYear", R.string.timeAgoYear);
        } else {
            return LocaleController.formatString("timeAgoYears", R.string.timeAgoYears, (int) Math.round(years));
        }
    }
}