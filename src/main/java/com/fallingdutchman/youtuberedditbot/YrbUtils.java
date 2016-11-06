package com.fallingdutchman.youtuberedditbot;

import com.fallingdutchman.youtuberedditbot.model.YoutubeVideo;
import com.google.api.client.util.DateTime;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;

/**
 * Created by Douwe Koopmans on 22-1-16.
 */
@UtilityClass
public final class YrbUtils {
    public static final String LOCAL_HOST_FOLDER = "data/";

    public static LocalDateTime dateToLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public static LocalDateTime dateTimeToLocalDateTime(DateTime dt) {
        return Instant.ofEpochMilli(dt.getValue()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Date localDateToDate(LocalDateTime date) {
        return Date.from(date.atZone(ZoneId.systemDefault()).toInstant());
    }

    @NonNull
    public static Comparator<Optional<YoutubeVideo>> getOptionalVideoComparator() {
        return (o1, o2) -> {
            if (!o1.isPresent() && !o2.isPresent()) {
                return 0;
            } else if (!o1.isPresent()) {
                return -1;
            } else if (!o2.isPresent()) {
                return 1;
            } else {
                return o2.get().compareTo(o1.get());
            }
        };
    }
}
