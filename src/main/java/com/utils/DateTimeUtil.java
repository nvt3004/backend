package com.utils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class DateTimeUtil {

    private static final String VIETNAM_TIMEZONE = "Asia/Ho_Chi_Minh";

    public static ZonedDateTime getCurrentDateTimeInVietnam() {
        return ZonedDateTime.now(ZoneId.of(VIETNAM_TIMEZONE)).plusHours(7);
    }

    public static Date getCurrentDateInVietnam() {
        ZonedDateTime zdt = getCurrentDateTimeInVietnam();
        return Date.from(zdt.toInstant());
    }
}

