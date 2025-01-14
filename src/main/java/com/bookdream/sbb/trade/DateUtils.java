package com.bookdream.sbb.trade;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class DateUtils {

    public static String formatDateTime(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);

        if (dateTime.toLocalDate().isEqual(now.toLocalDate())) {
            return dateTime.format(DateTimeFormatter.ofPattern("a hh:mm").withLocale(Locale.KOREAN));
        } else if (dateTime.toLocalDate().isEqual(yesterday.toLocalDate())) {
            return "어제";
        } else {
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }
}