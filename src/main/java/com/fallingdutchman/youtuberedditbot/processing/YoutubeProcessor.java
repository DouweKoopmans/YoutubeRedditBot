package com.fallingdutchman.youtuberedditbot.processing;

import com.fallingdutchman.youtuberedditbot.YoutubeVideo;
import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManager;
import com.fallingdutchman.youtuberedditbot.formatting.FileFormatterFactory;
import com.fallingdutchman.youtuberedditbot.formatting.Formatter;
import com.fallingdutchman.youtuberedditbot.formatting.FormatterFactory;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.Submission;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

/**
 * Created by douwe on 2-10-16.
 */
@Slf4j
public final class YoutubeProcessor {
    private final YoutubeVideo video;
    private final RedditManager reddit;

    private FormatterFactory formatterFactory;

    public YoutubeProcessor(YoutubeVideo video, RedditManager reddit) {
        this.video = video;
        this.reddit = reddit;
        this.formatterFactory = new FileFormatterFactory();
    }

    public synchronized Optional<Submission> postVideo(String subreddit, boolean selfPost, Runnable listener) {
        if (selfPost) {
            throw new UnsupportedOperationException("don't support self posts");
        } else {
            try {
                return this.reddit.submitPost(video.getVideoTitle(), video.getUrl(), subreddit);
            } catch (NetworkException e) {
                log.error("post attempt was unsuccessful", e);
                log.warn("attempting reauthentication");
                listener.run();
                try {
                    return this.reddit.submitPost(video.getVideoTitle(), video.getUrl(), subreddit);
                } catch (NetworkException ex) {
                    log.error("reauthentication attempt failed", ex);
                    return Optional.empty();
                }
            }
        }
    }

    public synchronized Optional<String> postComment(Submission submission, String formatName) {
        final HashMap<String, String> values = Maps.newHashMap();
        values.put("title", video.getVideoTitle());
        values.put("channelId", video.getChannelId());
        values.put("publishDate", video.getPublishDate().toString());
        values.put("videoId", video.getVideoId());
        values.put("description", video.getDescription());

        final Formatter formatter;
        try {
            formatter = formatterFactory.createFormatterFromName(formatName);
        } catch (IOException e) {
            log.error(String.format("was unable to create Formatter from %s", formatName), e);
            return Optional.empty();
        }

        return this.reddit.submitComment(formatter.format(values), submission);
    }
}
