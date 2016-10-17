package com.fallingdutchman.youtuberedditbot;

import lombok.Data;

import java.net.URL;
import java.time.LocalDateTime;

/**
 * Created by Douwe Koopmans on 8-1-16.
 */
@Data
public class YoutubeVideo {
    private final String videoTitle;
    private final String videoId;
    private final URL url;
    private final LocalDateTime publishDate;
    private final String channelId;
    private final String description;


    public YoutubeVideo(String videoTitle, String videoId, URL url, String description, LocalDateTime publishDate,
                        String channelId) {
        this.videoTitle = videoTitle;
        this.videoId = videoId;
        this.url = url;
        this.description = description;
        this.publishDate = publishDate;
        this.channelId = channelId;
    }
}
