package com.fallingdutchman.youtuberedditbot.processing;

import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManager;
import com.fallingdutchman.youtuberedditbot.formatting.FileFormatterFactory;
import com.fallingdutchman.youtuberedditbot.formatting.Formatter;
import com.fallingdutchman.youtuberedditbot.formatting.FormatterFactory;
import com.fallingdutchman.youtuberedditbot.model.CommentRule;
import com.fallingdutchman.youtuberedditbot.model.YoutubeVideo;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.Submission;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Created by douwe on 2-10-16.
 */
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class YoutubeProcessor {
    @NonNull YoutubeVideo video;
    @NonNull RedditManager reddit;
    @NonNull List<CommentRule> commentRules;
    @NonNull FormatterFactory formatterFactory = new FileFormatterFactory();

    public synchronized Optional<Submission> postVideo(String subreddit, boolean selfPost) {
        if (selfPost) {
            throw new UnsupportedOperationException("don't support self posts");
        } else {
            try {
                return this.reddit.submitPost(video.getVideoTitle(), video.getUrl(), subreddit);
            } catch (NetworkException e) {
                log.error("post attempt was unsuccessful", e);
                log.warn("attempting reauthentication");
                reddit.reauthenticate();
                try {
                    return this.reddit.submitPost(video.getVideoTitle(), video.getUrl(), subreddit);
                } catch (NetworkException ex) {
                    log.error("reauthentication attempt failed", ex);
                    return Optional.empty();
                }
            }
        }
    }

    public synchronized Optional<String> postComment(Submission submission, String formatPath) {
        final HashMap<String, String> values = Maps.newHashMap();
        values.put("title", video.getVideoTitle());
        values.put("publishDate", video.getPublishDate().toString());
        values.put("videoId", video.getVideoId());
        String description = video.getDescription();
        for (CommentRule commentRule : commentRules) {
            description = description.replaceAll(commentRule.getFind(), commentRule.getReplace());
        }
        values.put("description", description);

        final Formatter formatter;
        try {
            formatter = formatterFactory.createFormatterFromPath(formatPath);
        } catch (IOException e) {
            log.error("was unable to create Formatter from {}", formatPath, e);
            return Optional.empty();
        }

        try {
            return this.reddit.submitComment(formatter.format(values), submission);
        } catch (NetworkException e) {
            log.error("comment attempt was unsuccessful", e);
            log.warn("attempting reauthentication");
            reddit.reauthenticate();
            try {
                return this.reddit.submitComment(formatter.format(values), submission);
            } catch (NetworkException ex) {
                log.error("reauthentication attempt failed", ex);
                return Optional.empty();
            }
        }
    }
}
