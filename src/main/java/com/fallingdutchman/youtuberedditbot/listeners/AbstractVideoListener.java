package com.fallingdutchman.youtuberedditbot.listeners;

import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManager;
import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManagerRegistry;
import com.fallingdutchman.youtuberedditbot.listeners.filtering.FilterFactory;
import com.fallingdutchman.youtuberedditbot.listeners.filtering.VideoFilter;
import com.fallingdutchman.youtuberedditbot.model.AppConfig;
import com.fallingdutchman.youtuberedditbot.model.Instance;
import com.fallingdutchman.youtuberedditbot.model.Video;
import com.fallingdutchman.youtuberedditbot.processing.ProcessorFactory;
import com.fallingdutchman.youtuberedditbot.processing.VideoProcessor;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by douwe on 19-10-16.
 */
@Slf4j
@ToString(exclude = {"timer", "redditManager"}, doNotUseGetters = true)
@EqualsAndHashCode(exclude = "timer", callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class AbstractVideoListener<E> extends TimerTask {
    @Getter
    final Instance instance;
    final RedditManager redditManager;
    final VideoProcessor processor;
    final VideoFilter filter;
    protected final AppConfig config;
    final List<Video> videos;
    @Getter(AccessLevel.PRIVATE)
    LocalDateTime latestVideo = LocalDateTime.now();
    Timer timer;
    @Getter
    @Setter(AccessLevel.PROTECTED)
    boolean listening;


    @Inject
    AbstractVideoListener(@Assisted @NonNull Instance configInstance, ProcessorFactory processorFactory,
                          AppConfig config, RedditManagerRegistry redditManagerRegistry, FilterFactory filterFactory) {
        this.instance = configInstance;
        this.processor = processorFactory.create(this.instance);
        this.filter = filterFactory.create(instance);
        this.redditManager = redditManagerRegistry.addManager(instance.getRedditCredentials().getRedditUsername(),
                configInstance.getRedditCredentials());
        this.config = config;
        this.videos = Lists.newArrayList();
    }


    public final void listen() throws Exception {
        if (isListening()) {
            throw new IllegalStateException("listener is already listening");
        }

        setListening(true);

        log.info("starting up new listener for {}", getInstance().getName());
        timer = new Timer(instance.getName());

        try {
            onListen();
            val period = (long) (getInstance().getInterval() * config.getListenerConfig().getIntervalStep());
            timer.schedule(this, 0, period * 1000);
        } catch (Exception e) {
            log.error("was unable to start listener for instance: " + instance.getName() + " because an " +
                    "exception was thrown", e);
            throw new Exception(e);
        }
    }

    public void stopListening() {
        if (!isListening()) {
            log.info("stopping listener for {}", getInstance().getChannelId());
            timer.cancel();
            timer.purge();
            timer = null;
            setListening(false);
        } else {
            throw new IllegalStateException("this listener (" + getInstance().getName() + ") isn't listening, you can't stop it");
        }
    }

    protected final synchronized List<Video> getVideos() {
        return ImmutableList.copyOf(videos);
    }

    final synchronized void setVideos(List<Video> list) {
        videos.clear();
        videos.addAll(list);
    }

    /**
     * called when starting listener.
     * <p>
     * override this method to add additional functionality when starting the listener. throw a RuntimeException
     * when for some reason the listener shouldn't or couldn't be started.
     *
     * @throws Exception when this listener shouldn't of couldn't start.
     */
    protected void onListen() throws Exception {
        if (!this.videos.isEmpty()) {
            val video = this.videos.get(0);
            this.setLatestVideo(video.getPublishDate());
        }
    }

    protected abstract Optional<List<E>> update();

    public abstract Optional<Video> extract(E target);

    void handleNewVideo(@NonNull Video video) {
        log.info("found a new video, \n {}", video.toString());
        getInstance().getSubreddit().forEach(subreddit -> this.processor.processVideo(video, subreddit));
    }

    @Override
    public final void run() {
        try {
            this.runTimer();
        } catch (Exception e) {
            log.error("something went wrong in the execution cycle", e);
        }
    }

    protected void setLatestVideo(LocalDateTime latestVideo) {
        if (latestVideo.isAfter(getLatestVideo())) {
            this.latestVideo = latestVideo;
        }
    }

    private void runTimer() {
        val stopwatch = Stopwatch.createStarted();
        final Optional<List<E>> update = this.update();
        update.ifPresent(updated -> {
            setVideos(processUpdate(updated));

            val videosFiltered = getVideos().stream().filter(video -> video.getPublishDate().isAfter(this.getLatestVideo()))
                    .collect(Collectors.toList());

            if (videosFiltered.size() > 0) {
                log.debug("poller for {} has found {} new videos", getInstance().getName(), videosFiltered.size());
            }

            videosFiltered.stream().findFirst().ifPresent(video -> setLatestVideo(video.getPublishDate()));
            videosFiltered.stream().filter(this.filter).forEach(this::handleNewVideo);
            videosFiltered.clear();
        });
        val elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);

        this.getVideos().stream().findFirst().ifPresent(video ->
                log.debug("finished polling in {} milliseconds. latest entry:  {id={}, date={}}",
                        elapsed, video.getVideoId(), video.getPublishDate()));
    }

    private List<Video> processUpdate(List<E> update) {
        return update.stream()
                .filter(Objects::nonNull)
                .map(this::extract)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted()
                .collect(Collectors.toList());
    }
}