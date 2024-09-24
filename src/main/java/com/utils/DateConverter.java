package com.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateConverter {

    private static final String DATE_FORMAT = "EEE MMM dd yyyy HH:mm:ss 'GMT'Z";
    private static final String DATE_FORMAT2 = "yyyy-MM-dd HH:mm:ss.S";

    public static Timestamp convertToTimestamp(String dateStr) throws ParseException {
        try {
            String cleanedDateStr = dateStr.replaceAll("\\(.*\\)", "").trim();

            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT")); 
            Date date = sdf.parse(cleanedDateStr); 
            return new Timestamp(date.getTime()); 
        } catch (ParseException e) {
            SimpleDateFormat sdf2 = new SimpleDateFormat(DATE_FORMAT2);
            Date date2 = sdf2.parse(dateStr);
            return new Timestamp(date2.getTime()); 
        }
    }
}
