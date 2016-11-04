package com.fallingdutchman.youtuberedditbot;

import lombok.Data;

import javax.annotation.Nonnull;
import java.net.URL;
import java.time.LocalDateTime;

/**
 * Created by Douwe Koopmans on 8-1-16.
 */
@Data
public class YoutubeVideo implements Comparable<YoutubeVideo>{
    private final String videoTitle;
    private final String videoId;
    private final URL url;
    private final LocalDateTime publishDate;
    private final String description;


    public YoutubeVideo(String videoTitle, String videoId, URL url, String description, LocalDateTime publishDate) {
        this.videoTitle = videoTitle;
        this.videoId = videoId;
        this.url = url;
        this.description = description;
        this.publishDate = publishDate;
    }

    @Override
    public int compareTo(@Nonnull YoutubeVideo o) {
        return o.getPublishDate().compareTo(publishDate);
    }

}
