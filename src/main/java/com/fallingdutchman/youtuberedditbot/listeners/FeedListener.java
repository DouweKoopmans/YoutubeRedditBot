package com.fallingdutchman.youtuberedditbot.listeners;

import com.fallingdutchman.youtuberedditbot.YoutubeVideo;
import com.fallingdutchman.youtuberedditbot.model.Instance;
import com.fallingdutchman.youtuberedditbot.polling.AbstractPoller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Created by Douwe Koopmans on 10-1-16.
 */
public interface FeedListener<E> {

    void stopListening();

    void listen();

    void newVideoPosted(YoutubeVideo video);

    boolean update();

    LocalDateTime getLatestVideo();

    Instance getInstance();

    AbstractPoller getPoller();

    void setLatestVideo(LocalDateTime date);

    List<YoutubeVideo> getVideos();

    Optional<YoutubeVideo> extract(E target);
}
