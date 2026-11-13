package com.oslab.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class FormatUtils {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("MMM d, h:mm a");

    private FormatUtils() {
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return TIME_FORMAT.format(dateTime);
    }
}
