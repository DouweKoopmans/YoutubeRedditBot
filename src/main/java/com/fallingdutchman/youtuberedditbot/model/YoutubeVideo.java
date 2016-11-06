package com.fallingdutchman.youtuberedditbot.model;

import lombok.NonNull;
import lombok.ToString;
import lombok.Value;

import java.net.URL;
import java.time.LocalDateTime;

/**
 * Created by Douwe Koopmans on 8-1-16.
 */
@Value
@ToString(exclude = "description")
public class YoutubeVideo implements Comparable<YoutubeVideo>{
    @NonNull String videoTitle;
    @NonNull String videoId;
    @NonNull URL url;
    @NonNull LocalDateTime publishDate;
    @NonNull String description;

    @Override
    public int compareTo(@NonNull YoutubeVideo o) {
        return o.getPublishDate().compareTo(getPublishDate());
    }

}
