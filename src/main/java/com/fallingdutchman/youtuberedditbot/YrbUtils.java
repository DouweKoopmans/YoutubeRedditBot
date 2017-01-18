package com.fallingdutchman.youtuberedditbot;

import com.fallingdutchman.youtuberedditbot.model.YoutubeVideo;
import com.google.api.client.util.DateTime;
import com.typesafe.config.Config;
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
public class YrbUtils {

    public LocalDateTime dateToLocalDateTime(@NonNull Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public LocalDateTime dateTimeToLocalDateTime(@NonNull DateTime dt) {
        return Instant.ofEpochMilli(dt.getValue()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public Date localDateToDate(@NonNull LocalDateTime date) {
        return Date.from(date.atZone(ZoneId.systemDefault()).toInstant());
    }

    @SuppressWarnings("OptionalIsPresent")
    public Comparator<Optional<YoutubeVideo>> getOptionalVideoComparator() {
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

    @NonNull
    public String getPathOrDefault(Config config, String path, String defaultValue) {
        return config.hasPathOrNull(path) ? config.getString(path) : defaultValue;
    }

    @NonNull
    public long getPathOrDefault(Config config, String path, long defaultValue) {
        return config.hasPathOrNull(path) ? config.getLong(path) : defaultValue;
    }

    @NonNull
    public double getPathOrDefault(Config config, String path, double defaultValue) {
        return config.hasPathOrNull(path) ? config.getDouble(path) : defaultValue;
    }

    @NonNull
    public int getPathOrDefault(Config config, String path, int defaultValue) {
        return config.hasPathOrNull(path) ? config.getInt(path) : defaultValue;
    }

    @NonNull
    public boolean getPathOrDefault(Config config, String path, boolean defaultValue) {
        return config.hasPathOrNull(path) ? config.getBoolean(path) : defaultValue;
    }
}
