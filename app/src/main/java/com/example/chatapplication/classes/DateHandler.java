package com.example.chatapplication.classes;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateHandler {

    public static String GetDateString(Date date, String format) {

        DateFormat parser = new SimpleDateFormat(format);

        try {
            return parser.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String FormatDate4(Object o, String inPattern, String outPattern) {
        SimpleDateFormat parser = new SimpleDateFormat(inPattern);

        SimpleDateFormat parser2 = new SimpleDateFormat(outPattern, Locale.ENGLISH);
//        parser.setTimeZone(TimeZone.getTimeZone());
        try {
            Date date = parser.parse(o.toString());

            return parser2.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String covertDateToAgo(Date date) {

        String convTime = null;

        String prefix = "";
        String suffix = "Ago";

        try {
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//            Date pasTime = dateFormat.parse(dataDate);

            Date nowTime = new Date();

            long dateDiff = nowTime.getTime() - date.getTime();
            if (dateDiff < 0)
                return convTime;
            long second = TimeUnit.MILLISECONDS.toSeconds(dateDiff);
            long minute = TimeUnit.MILLISECONDS.toMinutes(dateDiff);
            long hour = TimeUnit.MILLISECONDS.toHours(dateDiff);
            long day = TimeUnit.MILLISECONDS.toDays(dateDiff);

            if (second < 60) {
                convTime = second + " Seconds " + suffix;
            } else if (minute < 60) {
                convTime = minute + " Minutes " + suffix;
            } else if (hour < 24) {
                convTime = hour + " Hours " + suffix;
            } else if (day >= 7) {
                if (day > 360) {
                    convTime = (day / 360) + " Years " + suffix;
                } else if (day > 30) {
                    convTime = (day / 30) + " Months " + suffix;
                } else {
                    convTime = (day / 7) + " Week " + suffix;
                }
            } else if (day < 7) {
                convTime = day + " Days " + suffix;
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ConvTimeE", e.getMessage());
        }

        return convTime;
    }

}
