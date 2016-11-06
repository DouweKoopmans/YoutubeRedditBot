package com.fallingdutchman.youtuberedditbot.listeners;

import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManager;
import com.fallingdutchman.youtuberedditbot.config.ConfigHandler;
import com.fallingdutchman.youtuberedditbot.model.Instance;
import com.fallingdutchman.youtuberedditbot.model.YoutubeVideo;
import com.fallingdutchman.youtuberedditbot.polling.AbstractPoller;
import com.fallingdutchman.youtuberedditbot.polling.DefaultNewVideoPoller;
import com.fallingdutchman.youtuberedditbot.polling.DescriptionListenerPoller;
import com.fallingdutchman.youtuberedditbot.processing.YoutubeProcessor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.dean.jraw.models.Submission;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Timer;
import java.util.function.Consumer;

/**
 * Created by douwe on 19-10-16.
 */
@Slf4j
@ToString(exclude = {"timer", "authenticator", "poller"}, doNotUseGetters = true)
@EqualsAndHashCode(exclude = "timer")
public abstract class AbstractYoutubeListener<E> implements FeedListener<E> {
    @NonNull private final RedditManager authenticator;
    @NonNull private final Instance instance;
    @NonNull private final AbstractPoller poller;
    private LocalDateTime latestVideo = LocalDateTime.now();
    private Timer timer;

    AbstractYoutubeListener(@NonNull RedditManager authenticator,@NonNull Instance instance) throws IOException {
        this.authenticator = authenticator;
        this.instance = instance;
        this.authenticator.authenticate(ConfigHandler.getInstance().getRedditCredentials());

        poller = createPoller();
    }

    @Override
    public void listen() {
        log.info("starting up new listener for {}", getInstance().getChannelId());
        timer = new Timer();

        try {
            if (update() && onListen()) {
                val period = (long) (getInstance().getPollerInterval() * 60);
                timer.schedule(getPoller(), 0, period * 1000);
            } else {
                log.warn("was unable to initiate the feed will not start the poller for {}", this.toString());
            }
        } catch (Exception e) {
            log.error("an error occurred whilst trying to Listen to the feed: ", e);
        }
    }

    abstract boolean onListen();

    @Override
    public void stopListening() {
        log.info("stopping listener for {}", getInstance().getChannelId());
        timer.cancel();
    }

    @Override
    public void newVideoPosted(@NonNull final YoutubeVideo video) {
        log.info("found a new video, \n {}", video.toString());
        this.setLatestVideo(video.getPublishDate());
        YoutubeProcessor processor = new YoutubeProcessor(video, authenticator, instance.getCommentRules());

        log.info("processing video, id=\"{}\"", video.getVideoId());
        getInstance().getSubreddits().forEach(processVideo(processor));
    }

    @Override
    public LocalDateTime getLatestVideo() {
        return latestVideo;
    }

    @Override
    public Instance getInstance() {
        return instance;
    }

    @Override
    public AbstractPoller getPoller() {
        return poller;
    }

    @Override
    public void setLatestVideo(@NonNull final LocalDateTime date) {
        this.latestVideo = date;
        log.debug("setting latest video date of {} to {}", this.instance.getChannelId(), date);
    }

    @Nonnull
    private Consumer<String> processVideo(@NonNull final YoutubeProcessor processor) {
        return subreddit -> {
            log.debug("processing new video for /r/{}", subreddit);
            Optional<Submission> submission = processor.postVideo(subreddit, false,
                    () -> this.authenticator.authenticate(ConfigHandler.getInstance().getRedditCredentials()));
            if (submission.isPresent() && instance.isPostComment()) {
                processor.postComment(submission.get(), instance.getCommentFormatPath());
            }
        };
    }

    private AbstractPoller createPoller() {
        switch (instance.getPollerType()) {
            case "description-mention":
                return new DescriptionListenerPoller(this);
            case "new-video":
            default:
                return new DefaultNewVideoPoller(this);
        }
    }
}
