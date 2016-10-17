package com.fallingdutchman.youtuberedditbot.polling;

import com.fallingdutchman.youtuberedditbot.YoutubeFeedListener;
import com.fallingdutchman.youtuberedditbot.YoutubeVideo;
import com.rometools.rome.feed.synd.SyndEntry;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Created by Douwe Koopmans on 10-1-16.
 */
@Slf4j
public class DefaultNewVideoPoller extends AbstractPoller {

    public DefaultNewVideoPoller(YoutubeFeedListener listener) {
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
            log.trace("%s th iteration of poller", i);
            SyndEntry entry = listener.getFeedEntries().get(i);
            log.trace("entry for iteration %s:", i);
            log.trace(entry.toString());
            final Optional<YoutubeVideo> video = listener.find(entry);
            if (video.isPresent()) {
                log.trace("video found for iteration %s:", i);
                log.trace(video.get().toString());
                this.listener.newVideoPosted(video.get());
            } else {
                log.error("was unable to extract video from rss feed, feed might have been malformed in some way");
            }
        }
    }
}
