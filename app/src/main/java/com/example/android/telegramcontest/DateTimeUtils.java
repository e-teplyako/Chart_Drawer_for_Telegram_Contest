package com.example.android.telegramcontest;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {

    public static Date fromTimestamp(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    public static String formatDate(Long timestamp) {
        Date date = fromTimestamp(timestamp);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
        return dateFormat.format(date);
    }

}
