package com.example.android.telegramcontest.Utils;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {

    public static Date fromTimestamp(long timestamp) {
        return new Date(timestamp);
    }

    public static String formatDateMMMd(Long timestamp) {
        Date date = fromTimestamp(timestamp);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
        return dateFormat.format(date);
    }

    public static String formatDateEEEMMMd(long timestamp) {
        Date date = fromTimestamp(timestamp);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
        return dateFormat.format(date);
    }

}
