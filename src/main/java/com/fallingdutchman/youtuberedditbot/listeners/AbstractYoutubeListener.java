package com.fallingdutchman.youtuberedditbot.listeners;

import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManager;
import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManagerRegistry;
import com.fallingdutchman.youtuberedditbot.listeners.filtering.FilterFactory;
import com.fallingdutchman.youtuberedditbot.listeners.filtering.VideoFilter;
import com.fallingdutchman.youtuberedditbot.model.AppConfig;
import com.fallingdutchman.youtuberedditbot.model.Instance;
import com.fallingdutchman.youtuberedditbot.model.YoutubeVideo;
import com.fallingdutchman.youtuberedditbot.processing.ProcessorFactory;
import com.fallingdutchman.youtuberedditbot.processing.YoutubeProcessor;
import com.google.api.client.util.Lists;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by douwe on 19-10-16.
 */
@Slf4j
@ToString(exclude = {"timer", "redditManager"}, doNotUseGetters = true)
@EqualsAndHashCode(exclude = "timer", callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class AbstractYoutubeListener<E> extends TimerTask{

    @Getter
    final Instance instance;
    final RedditManager redditManager;
    final ProcessorFactory processorFactory;
    final VideoFilter filter;
    protected final AppConfig config;
    private List<YoutubeVideo> videos = Lists.newArrayList();
    @Getter(AccessLevel.PRIVATE)
    @Setter LocalDateTime latestVideo = LocalDateTime.now();
    Timer timer;
    @Getter
    @Setter(AccessLevel.PROTECTED)
    boolean listening;

    @Inject
    AbstractYoutubeListener(@Assisted @NonNull Instance configInstance, ProcessorFactory processorFactory,
                            AppConfig config, RedditManagerRegistry redditManagerRegistry, FilterFactory filterFactory)
            throws IOException {
        this.instance = configInstance;
        this.filter = filterFactory.create(instance);
        this.processorFactory = processorFactory;
        this.redditManager = redditManagerRegistry.getManager(instance.getRedditCredentials().getRedditUsername());
        this.config = config;
    }


    public final void listen() {
        if (isListening()) {
            throw new IllegalStateException("listener is already listening");
        }

        setListening(true);

        log.info("starting up new listener for {}", getInstance().getChannelId());
        timer = new Timer();
        redditManager.authenticate(instance.getRedditCredentials());

        try {
            if (update() && onListen()) {
                val period = (long) (getInstance().getInterval() * config.getListenerConfig().getIntervalStep());
                timer.schedule(this, 0, period * 1000);
            } else {
                log.warn("was unable to initiate the feed will not start the poller for {}", this.toString());
            }
        } catch (Exception e) {
            log.error("an error occurred whilst trying to Listen to the feed: ", e);
        }
    }

    public void stopListening() {
        if (!isListening()) {
            log.info("stopping listener for {}", getInstance().getChannelId());
            timer.cancel();

            timer = null;
            setListening(false);
        } else {
            throw new IllegalStateException("this listener isn't listening, you can't stop it");
        }
    }

    protected final synchronized List<YoutubeVideo> getVideos() {
        return ImmutableList.copyOf(videos);
    }

    final synchronized void setVideos(List<YoutubeVideo> list) {
        this.videos.clear();
        videos = list;
    }

    @Nullable
    public abstract YoutubeVideo extract(@NonNull E target);

    protected boolean onListen() {
        if (!this.videos.isEmpty()) {
            val youtubeVideo = this.videos.get(0);
            this.setLatestVideo(youtubeVideo.getPublishDate());
        }

        return true;
    }

    protected abstract boolean update();


    private void handleNewVideo(@NonNull final YoutubeVideo video) {
        log.info("found a new video, \n {}", video.toString());
        final YoutubeProcessor processor = processorFactory.create(this.instance, video);
        getInstance().getSubreddit().forEach(processor::processVideo);
    }

    @Override
    public final void run() {
        val startTime = System.currentTimeMillis();
        if (this.update()) {
            val entries = scanForNewEntries(getVideos());

            if (entries > 0) {
                log.debug("poller for {} has found {} new videos", getInstance().getChannelId(), entries);

                final List<YoutubeVideo> youtubeVideos = this.videos.subList(0, entries);

                youtubeVideos.forEach(youtubeVideo -> this.setLatestVideo(youtubeVideo.getPublishDate()));
                youtubeVideos.stream()
                        .filter(this.filter)
                        .forEach(this::handleNewVideo);
            }

        } else {
            log.warn("something went wrong updating the feed, will not run poller");
        }

        String latestVideoId;

        if (this.videos.isEmpty()) {
          latestVideoId = null;
        } else {
          latestVideoId = this.videos.get(0).getVideoId();
        }

        log.debug("finished polling in {} milliseconds. latest entry:  {id={}, date={}}",
                System.currentTimeMillis() - startTime, latestVideoId, getLatestVideo());
    }

    /**
     * scan for new entries
     * @param entries the list of entries to scan
     * @return the number of new entries
     */
    final int scanForNewEntries(@NonNull final List<YoutubeVideo> entries) {
        int i = 0;

        for (YoutubeVideo entry : entries) {
            if (entry.getPublishDate().isAfter(this.getLatestVideo())) {
                i++;
            }
            else if (entry.getPublishDate().isEqual(this.getLatestVideo())) {
                break; //when we find the "latest date" we know that every entry after that will be older an thus irrelevant
            }
        }
        return i;
    }
}
