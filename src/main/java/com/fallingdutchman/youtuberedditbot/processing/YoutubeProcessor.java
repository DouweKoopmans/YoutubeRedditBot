package com.fallingdutchman.youtuberedditbot.processing;

import com.fallingdutchman.youtuberedditbot.YoutubeVideo;
import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManager;
import com.fallingdutchman.youtuberedditbot.formatting.FileFormatterFactory;
import com.fallingdutchman.youtuberedditbot.formatting.Formatter;
import com.fallingdutchman.youtuberedditbot.formatting.FormatterFactory;
import com.google.common.collect.Maps;
import net.dean.jraw.models.Submission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

/**
 * Created by douwe on 2-10-16.
 */
public final class YoutubeProcessor {
    private static final Logger log = LoggerFactory.getLogger(YoutubeProcessor.class);

    private final YoutubeVideo video;
    private final RedditManager reddit;

    private FormatterFactory formatterFactory;

    public YoutubeProcessor(YoutubeVideo video, RedditManager reddit) {
        this.video = video;
        this.reddit = reddit;
        this.formatterFactory = new FileFormatterFactory();
    }

    public synchronized Optional<Submission> postVideo(String subreddit, boolean selfPost) {
        if (selfPost) {
            throw new UnsupportedOperationException("don't support self posts");
        } else {
            return this.reddit.submitPost(video.getVideoTitle(), video.getUrl(), subreddit);
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
