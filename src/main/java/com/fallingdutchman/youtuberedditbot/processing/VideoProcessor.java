package com.fallingdutchman.youtuberedditbot.processing;

import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManager;
import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManagerRegistry;
import com.fallingdutchman.youtuberedditbot.formatting.FileFormatterFactory;
import com.fallingdutchman.youtuberedditbot.formatting.FormatterFactory;
import com.fallingdutchman.youtuberedditbot.model.AppConfig;
import com.fallingdutchman.youtuberedditbot.model.Instance;
import com.fallingdutchman.youtuberedditbot.model.Video;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.overzealous.remark.Remark;
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

/**
 * Created by douwe on 2-10-16.
 */
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VideoProcessor {
    RedditManager reddit;
    Instance configInstance;
    FormatterFactory formatterFactory;

    @Inject
    public VideoProcessor(@NonNull @Assisted Instance configInstance, @NonNull AppConfig config,
                          @NonNull RedditManagerRegistry redditRegistry) {
        this.reddit = redditRegistry.addManager(configInstance.getRedditCredentials().getRedditUsername(),
                configInstance.getRedditCredentials());
        this.configInstance = configInstance;
        this.formatterFactory = new FileFormatterFactory(config);
    }

    public void processVideo(final Video video, final String subreddit) {
        log.info("processing video, id=\"{}\", subreddit: \"{}\"", video.getVideoId(), subreddit);
        val submission = this.postVideo(video, subreddit, false);
        if (submission.isPresent()) {
            if (configInstance.getComment().isPostComment()) {
                val commentResult = this.postComment(submission.get(), configInstance.getComment().getFormatPath(),
                        video);

                if (commentResult.isPresent()) {
                    val timeSpent = ChronoUnit.SECONDS.between(video.getPublishDate(), LocalDateTime.now());
                    log.info("successfully processed \"{}\", it took {} seconds to detect and process the video",
                            video.getVideoId(), timeSpent);
                }
            }
        }
    }

    @Synchronized
    protected Optional<Submission> postVideo(Video video, String subreddit, boolean selfPost) {
        if (selfPost) {
            throw new UnsupportedOperationException("self posts are not supported");
        } else {
            return Optional.ofNullable(this.reddit.submitPost(video.getVideoTitle(), video.getUrl(), subreddit));
        }
    }

    protected synchronized Optional<String> postComment(Submission submission, String formatPath, Video video) {
        final String commentText;
        try {
            commentText = generateCommentText(formatPath, video);
        } catch (IOException e) {
            log.error("was unable to create Formatter from {}", formatPath, e);
            return Optional.empty();
        }

        return Optional.ofNullable(this.reddit.submitComment(commentText, submission));
    }

    private String generateCommentText(final String formatPath, final Video video) throws IOException {
        String description = video.getDescription();

        if (configInstance.getType().equalsIgnoreCase("twitch")) {
            description = new Remark().convert(description);
        }

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
}
