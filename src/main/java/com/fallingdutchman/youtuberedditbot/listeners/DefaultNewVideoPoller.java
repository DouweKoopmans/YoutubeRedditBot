package com.fallingdutchman.youtuberedditbot.listeners;

import com.fallingdutchman.youtuberedditbot.YoutubeVideo;
import com.rometools.rome.feed.synd.SyndEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Douwe Koopmans on 10-1-16.
 */
public class DefaultNewVideoPoller extends AbstractPoller {
    private static final Logger log = LoggerFactory.getLogger(DefaultNewVideoPoller.class);

    public DefaultNewVideoPoller(FeedListener listener) {
        super(listener);
    }

    @Override
    protected void runPoller(final int entries) {
        if (entries > 0) {
            log.trace("running poller with %s entries", entries);
        }
        //this is a reverse loop so it iterates over the videos from oldest to newest, this is needed so the latestvideo
        //object stored in the listener is set to the newest and not the oldest video
        for (int i = entries - 1; i >= 0; i--) {
            log.trace("%s th iteration of poller", i);
            SyndEntry entry = listener.getFeed().getEntries().get(i);
            log.trace("entry for iteration %s:", i);
            log.trace(entry.toString());
            YoutubeVideo video = listener.find(entry);
            log.trace("video found for iteration %s:", i);
            log.trace(video.toString());
            this.listener.newVideoPosted(video);
        }
    }
}
