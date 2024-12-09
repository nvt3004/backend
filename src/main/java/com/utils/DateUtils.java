package com.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
	public static String formatDate(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		return formatter.format(date);
	}

	
	public static String formatDateString(Date date, String format) {
        if (date == null || format == null || format.isEmpty()) {
            return "";
        }

        try {
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            return formatter.format(date);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid date format: " + format);
            return "";
        }
    }
}
