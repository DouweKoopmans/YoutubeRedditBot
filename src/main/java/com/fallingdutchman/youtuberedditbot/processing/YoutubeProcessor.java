package com.fallingdutchman.youtuberedditbot.processing;

import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManager;
import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManagerRegistry;
import com.fallingdutchman.youtuberedditbot.formatting.FileFormatterFactory;
import com.fallingdutchman.youtuberedditbot.formatting.FormatterFactory;
import com.fallingdutchman.youtuberedditbot.history.HistoryManager;
import com.fallingdutchman.youtuberedditbot.model.AppConfig;
import com.fallingdutchman.youtuberedditbot.model.Instance;
import com.fallingdutchman.youtuberedditbot.model.Post;
import com.fallingdutchman.youtuberedditbot.model.YoutubeVideo;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Synchronized;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.Submission;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Created by douwe on 2-10-16.
 */
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class YoutubeProcessor {
    protected static final String YOUTUBE_LINK_PATTERN = "(?:https?://)?(?:www\\.)?youtu(?:be\\.com/watch\\?v=|\\.be/)([-_A-Za-z0-9]{10}[AEIMQUYcgkosw048])";
    YoutubeVideo video;
    RedditManager reddit;
    Instance configInstance;
    FormatterFactory formatterFactory;
    protected HistoryManager historyManager;

    @Inject
    public YoutubeProcessor(@Assisted @NonNull YoutubeVideo video, @NonNull @Assisted Instance configInstance,
                            @NonNull AppConfig config, @NonNull RedditManagerRegistry redditRegistry,
                            @NonNull HistoryManager historyManager) {
        this.video = video;
        this.historyManager = historyManager;
        this.reddit = redditRegistry.getManager(configInstance.getRedditCredentials().getRedditUsername());
        this.configInstance = configInstance;
        this.formatterFactory = new FileFormatterFactory(config);
    }

    public void processVideo(final String subreddit) {
        log.info("processing video, id=\"{}\", subreddit: \"{}\"", video.getVideoId(), subreddit);
        val submission = this.postVideo(subreddit, false);
        if (submission.isPresent()) {
            try {
                historyManager.addPost(video, submission.get());
            } catch (IOException e) {
                log.error(String.format("was unable to save submission to history (video = %s)", video), e);
            }
            if (configInstance.getComment().isPostComment()) {
                val commentResult = this.postComment(submission.get(), configInstance.getComment().getFormatPath());

                if (commentResult.isPresent()) {
                    val timeSpent = ChronoUnit.SECONDS.between(this.video.getPublishDate(), LocalDateTime.now());
                    log.info("successfully processed \"{}\", it took {} seconds to detect and process the video",
                            this.video.getVideoId(), timeSpent);
                }
            }
        }
    }

    @Synchronized
    private Optional<Submission> postVideo(String subreddit, boolean selfPost) {
        if (selfPost) {
            throw new UnsupportedOperationException("self posts are not supported");
        } else {
            try {
                return Optional.ofNullable(this.reddit.submitPost(video.getVideoTitle(), video.getUrl(), subreddit));
            } catch (NetworkException e) {
                //TEST ME
                log.warn("an error occurred whilst attempting to submit a new reddit post. error message: {}",
                        e.getMessage());

                if (e.getResponse().getStatusCode() == 401) {
                    log.info("attempting reauthentication");
                    reddit.reauthenticate();

                    log.debug("retrying to submit post");
                    try {
                        return Optional.ofNullable(this.reddit.submitPost(video.getVideoTitle(), video.getUrl(), subreddit));
                    } catch (NetworkException ex) {
                        log.error("was unable to submit reddit post for \"{}\"", video.getVideoId());
                        log.error("initial error:", e);
                        log.error("second error:", ex);

                        return Optional.empty();
                    }

                } else {
                    log.error(String.format("was unable to submit reddit post for \"%s\"", video.getVideoId()), e);
                    return Optional.empty();
                }
            }
        }
    }

    private synchronized Optional<String> postComment(Submission submission, String formatPath) {
        final String commentText;
        try {
            commentText = generateCommentText(formatPath);
        } catch (IOException e) {
            log.error("was unable to create Formatter from {}", formatPath, e);
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(this.reddit.submitComment(commentText, submission));
        } catch (NetworkException e) {
            log.warn("an error occurred whilst attempting to submit a new reddit comment. error message: {}", e.getMessage());

            if (e.getResponse().getStatusCode() == 401) {
                log.info("attempting reauthentication");
                reddit.reauthenticate();

                log.debug("retrying to submit comment");
                try {
                    return Optional.ofNullable(this.reddit.submitComment(commentText, submission));
                } catch (NetworkException ex) {
                    log.error("was unable to submit reddit comment for \"{}\"", video.getVideoId());
                    log.error("initial error:", e);
                    log.error("second error:", ex);

                    return Optional.empty();
                }

            } else {
                log.error("was unable to submit reddit comment", e);
                return Optional.empty();
            }
        }
    }

    private String generateCommentText(final String formatPath) throws IOException {
        String description = video.getDescription();
        description = replaceYtLinks(description);

        for (Instance.Comment.CommentRule commentRule : configInstance.getComment().getRules()) {
            description = description.replaceAll(commentRule.getFind(), commentRule.getReplace());
        }

        final HashMap<String, String> values = Maps.newHashMap();
        values.put("title", video.getVideoTitle());
        values.put("publishDate", video.getPublishDate().toString());
        values.put("videoId", video.getVideoId());
        values.put("description", description);

        return formatterFactory.createFormatterFromPath(formatPath).format(values);
    }

    protected String replaceYtLinks(@NonNull final String description) {
        String result = description;
        val matcher = Pattern.compile(YOUTUBE_LINK_PATTERN)
                .matcher(description);

        while(matcher.find()) {
            val group = matcher.group(1);
            val historyResult = historyManager.getHistory().stream()
                    .filter(p -> p.getVideo().getVideoId().equals(group))
                    .findFirst();

            if (historyResult.isPresent()) {
                final Post post = historyResult.get();
                final YoutubeVideo postVideo = post.getVideo();

                result = description.replace(matcher.group(), String.format("[%s](%s) \n" +
                        "[ ^^\\(reddit ^^discussion)](%s)  ", postVideo.getVideoTitle(), postVideo.getUrl().toExternalForm(),
                        post.getPermaLink()));
            }
        }

        return result;
    }
}
