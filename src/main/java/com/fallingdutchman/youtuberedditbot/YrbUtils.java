package com.fallingdutchman.youtuberedditbot;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Created by Douwe Koopmans on 22-1-16.
 */
public final class YrbUtils {
    private YrbUtils(){}

    // TESTME: 22-1-16
    public static LocalDateTime dateToLocalDate(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    // TESTME: 22-1-16
    public static Date localDateToDate(LocalDateTime date) {
        return Date.from(date.atZone(ZoneId.systemDefault()).toInstant());
    }
}
