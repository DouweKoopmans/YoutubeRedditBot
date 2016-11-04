package com.fallingdutchman.youtuberedditbot.polling;

import com.fallingdutchman.youtuberedditbot.YoutubeVideo;
import com.fallingdutchman.youtuberedditbot.listeners.FeedListener;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.TimerTask;

/**
 * Created by Douwe Koopmans on 10-1-16.
 */
@Slf4j
public abstract class AbstractPoller extends TimerTask{
    protected final FeedListener<?> listener;

    protected AbstractPoller(FeedListener listener) {
        this.listener = listener;
    }

    /**
     * scan for new entries
     * @param entries the list of entries to scan
     * @return the number of new entries
     */
    @VisibleForTesting
    int scanForNewEntries(List<YoutubeVideo> entries) {
        int i = 0;
        for (YoutubeVideo entry : entries) {
            if (entry.getPublishDate().isEqual(listener.getLatestVideo())) {
                break; //if we extract the "latest date" we know that every entry after that will be even older
            } else if (entry.getPublishDate().isAfter(listener.getLatestVideo())) {
                i++;
            }
        }
        return i;
    }

    @Override
    public final void run() {
        final long startTime = System.currentTimeMillis();
        if (listener.update()) {
            final YoutubeVideo latestVideo = listener.getVideos().get(0);

            log.debug("latest entry: {id={}, date={}}", latestVideo.getVideoId(), latestVideo.getPublishDate());

            final int entries = scanForNewEntries(listener.getVideos());

            if (entries > 0) {
                log.debug("poller for {} has found {} new videos", listener.getInstance().getChannelId(),
                        entries);
            }

            this.runPoller(entries);
        } else {
            log.warn("something went wrong updating the feed, will not run poller");
        }

        log.debug("finished polling in {} milliseconds", System.currentTimeMillis() - startTime);
    }

    protected abstract void runPoller(int entries);
}
