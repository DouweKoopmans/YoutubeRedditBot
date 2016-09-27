package com.fallingdutchman.youtuberedditbot;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Douwe Koopmans on 8-1-16.
 */
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

    public String getVideoTitle() {
        return videoTitle;
    }

    public String getVideoId() {
        return videoId;
    }

    public URL getUrl() {
        return url;
    }

    public LocalDateTime getPublishDate() {
        return publishDate;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getDescription() {
        return description;
    }

    // TESTME: 22-1-16
    @SuppressWarnings("InfiniteLoopStatement")
    public static List<String> extractDescription(String description, Pattern regex) {
        List<String> result = Lists.newArrayList();

        Matcher matcher = regex.matcher(description);
        if (matcher.find()) {
            try {
                //shut up
                for (int i = 0;; i++) {
                    result.set(i, matcher.group(i));
                }
            } catch (IndexOutOfBoundsException ignored) {
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("videoTitle", videoTitle)
                .add("videoId", videoId)
                .add("url", url)
                .add("publishDate", publishDate)
                .add("channelId", channelId)
                .add("description", description)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof YoutubeVideo)) return false;
        YoutubeVideo video = (YoutubeVideo) o;
        return Objects.equal(getVideoTitle(), video.getVideoTitle()) &&
                Objects.equal(getVideoId(), video.getVideoId()) &&
                Objects.equal(getUrl(), video.getUrl()) &&
                Objects.equal(getPublishDate(), video.getPublishDate()) &&
                Objects.equal(getChannelId(), video.getChannelId()) &&
                Objects.equal(getDescription(), video.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getVideoTitle(), getVideoId(), getUrl(), getPublishDate(), getChannelId(),
                getDescription());
    }
}
