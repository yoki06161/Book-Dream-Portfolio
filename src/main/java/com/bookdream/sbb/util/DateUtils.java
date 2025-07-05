package com.bookdream.sbb.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtils {
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);

        if (dateTime.toLocalDate().isEqual(now.toLocalDate())) {
            return dateTime.format(DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN));
        } else if (dateTime.toLocalDate().isEqual(yesterday.toLocalDate())) {
            return "어제";
        } else {
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }
}