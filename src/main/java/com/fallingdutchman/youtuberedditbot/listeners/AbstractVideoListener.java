package com.fallingdutchman.youtuberedditbot.listeners;

import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManager;
import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManagerRegistry;
import com.fallingdutchman.youtuberedditbot.history.HistoryManager;
import com.fallingdutchman.youtuberedditbot.listeners.filtering.FilterFactory;
import com.fallingdutchman.youtuberedditbot.listeners.filtering.VideoFilter;
import com.fallingdutchman.youtuberedditbot.model.AppConfig;
import com.fallingdutchman.youtuberedditbot.model.Instance;
import com.fallingdutchman.youtuberedditbot.model.Video;
import com.fallingdutchman.youtuberedditbot.processing.ProcessorFactory;
import com.fallingdutchman.youtuberedditbot.processing.VideoProcessor;
import com.google.api.client.util.Lists;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Collections;
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
public abstract class AbstractVideoListener<E> extends TimerTask{

    @Getter
    final Instance instance;
    final RedditManager redditManager;
    final ProcessorFactory processorFactory;
    final VideoFilter filter;
    final HistoryManager historyManager;
    protected final AppConfig config;
    private List<Video> videos = Lists.newArrayList();
    @Getter(AccessLevel.PRIVATE)
    LocalDateTime latestVideo = LocalDateTime.now();
    Timer timer;
    @Getter
    @Setter(AccessLevel.PROTECTED)
    boolean listening;


    @Inject
    AbstractVideoListener(@Assisted @NonNull Instance configInstance, ProcessorFactory processorFactory,
                          AppConfig config, RedditManagerRegistry redditManagerRegistry, FilterFactory filterFactory,
                          HistoryManager historyManager) {
        this.instance = configInstance;
        this.filter = filterFactory.create(instance);
        this.processorFactory = processorFactory;
        this.redditManager = redditManagerRegistry.getManager(instance.getRedditCredentials().getRedditUsername());
        this.config = config;
        this.historyManager = historyManager;
    }


    public final void listen() {
        if (isListening()) {
            throw new IllegalStateException("listener is already listening");
        }

        setListening(true);

        log.info("starting up new listener for {}", getInstance().getName());
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

    protected final synchronized List<Video> getVideos() {
        return ImmutableList.copyOf(videos);
    }

    final synchronized void setVideos(List<Video> list) {
        this.videos.clear();
        videos = list;
    }

    @Nullable
    public abstract Video extract(@NonNull E target);

    protected boolean onListen() {
        if (!this.videos.isEmpty()) {
            val video = this.videos.get(0);
            this.setLatestVideo(video.getPublishDate());
        }

        return true;
    }

    protected abstract boolean update();


    private void handleNewVideo(@NonNull final Video video) {
        log.info("found a new video, \n {}", video.toString());
        final VideoProcessor processor = processorFactory.create(this.instance, video);
        getInstance().getSubreddit().forEach(processor::processVideo);
    }

    @Override
    public final void run() {
        val startTime = System.currentTimeMillis();
        if (this.update()) {
            val entries = scanForNewEntries(getVideos());

            if (entries > 0) {
                log.debug("poller for {} has found {} new videos", getInstance().getChannelId(), entries);

                final List<Video> newVideos = this.videos.subList(0, entries);

                if (newVideos.size() > 1) {
                    // reverse list of entries so oldest video is processed first, if this is not done the latest video date
                    // will be set to the oldest video we just found instead of the newest
                    Collections.reverse(newVideos);
                }

                newVideos.forEach(video -> this.setLatestVideo(video.getPublishDate()));
                newVideos.stream()
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
    final int scanForNewEntries(@NonNull final List<Video> entries) {
        int i = 0;

        for (Video entry : entries) {
            if (entry.getPublishDate().isAfter(this.getLatestVideo())) {
                i++;
            }
            else if (entry.getPublishDate().isEqual(this.getLatestVideo())) {
                break; //when we find the "latest date" we know that every entry after that will be older an thus irrelevant
            }
        }
        return i;
    }

    public void setLatestVideo(LocalDateTime latestVideo) {
        if (latestVideo.isAfter(getLatestVideo())) {
            this.latestVideo = latestVideo;
        }
    }
}
