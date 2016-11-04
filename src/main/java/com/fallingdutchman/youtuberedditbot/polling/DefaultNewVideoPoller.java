package com.fallingdutchman.youtuberedditbot.polling;

import com.fallingdutchman.youtuberedditbot.YoutubeVideo;
import com.fallingdutchman.youtuberedditbot.listeners.AbstractYoutubeListener;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by Douwe Koopmans on 10-1-16.
 */
@Slf4j
public class DefaultNewVideoPoller extends AbstractPoller {

    public DefaultNewVideoPoller(AbstractYoutubeListener listener) {
        super(listener);
    }

    @Override
    protected void runPoller(final int entries) {
        if (entries > 0) {
            log.debug("running poller with {} entries", entries);
        }
        //this is a reverse loop so it iterates over the videos from oldest to newest, this is needed so the latestvideo
        //object stored in the listener is set to the newest and not the oldest video
        for (int i = entries - 1; i >= 0; i--) {
            log.trace("{} th iteration of poller", i);
            YoutubeVideo video = listener.getVideos().get(i);
            log.trace("video found for iteration {}:", i);
            log.trace(video.toString());
            this.listener.newVideoPosted(video);
        }
    }
}
