package org.gramity.calendar;

import android.content.Context;
import android.content.res.Resources;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeAgo {

    protected static Context context;

    public static String FastGet(String Dates) {
        TimeAgo s = new TimeAgo(ApplicationLoader.applicationContext);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(Dates);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String timeago = s.timeAgo(convertedDate);
        return timeago;
    }

    public TimeAgo(Context context) {
        this.context = context;
    }

    public String timeAgo(Date date) {
        return timeAgo(date.getTime());
    }

    public static String timeAgo(long millis) {
        long diff = new Date().getTime() - millis;
        if (context == null) {
            context = ApplicationLoader.applicationContext;
        }
        Resources r = context.getResources();

        String prefix = r.getString(R.string.time_ago_prefix);
        String suffix = r.getString(R.string.time_ago_suffix);

        double seconds = Math.abs(diff) / 1000;
        double minutes = seconds / 60;
        double hours = minutes / 60;
        double days = hours / 24;
        double years = days / 365;

        String words;

        if (seconds < 45) {
            words = r.getString(R.string.time_ago_seconds, (int) Math.round(seconds));
        } else if (seconds < 90) {
            words = r.getString(R.string.time_ago_minute, 1);
        } else if (minutes < 45) {
            words = r.getQuantityString(R.plurals.time_ago_minutes,
                    (int) Math.round(minutes), (int) Math.round(minutes));
        } else if (minutes < 90) {
            words = r.getString(R.string.time_ago_hour, 1);
        } else if (hours < 24) {
            words = r.getQuantityString(R.plurals.time_ago_hours,
                    (int) Math.round(hours), (int) Math.round(hours));
        } else if (hours < 42) {
            words = r.getString(R.string.time_ago_day, 1);
        } else if (days < 30) {
            words = r.getQuantityString(R.plurals.time_ago_days,
                    (int) Math.round(days), (int) Math.round(days));
        } else if (days < 45) {
            words = r.getString(R.string.time_ago_month, 1);
        } else if (days < 365) {
            words = r.getQuantityString(R.plurals.time_ago_months,
                    (int) Math.round(days / 30), (int) Math.round(days / 30));
        } else if (years < 1.5) {
            words = r.getString(R.string.time_ago_year, 1);
        } else {
            words = r.getQuantityString(R.plurals.time_ago_years,
                    (int) Math.round(years), (int) Math.round(years));
        }

        StringBuilder sb = new StringBuilder();

        if (prefix != null && prefix.length() > 0) {
            sb.append(prefix).append(" ");
        }

        sb.append(words);

        if (suffix != null && suffix.length() > 0) {
            sb.append(" ").append(suffix);
        }

        return sb.toString().trim();
    }
}