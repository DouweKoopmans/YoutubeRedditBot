package com.fallingdutchman.youtuberedditbot.polling;

import com.fallingdutchman.youtuberedditbot.listeners.FeedListener;
import com.fallingdutchman.youtuberedditbot.model.YoutubeVideo;
import com.google.common.annotations.VisibleForTesting;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;
import java.util.TimerTask;

/**
 * Created by Douwe Koopmans on 10-1-16.
 */
@Slf4j
public abstract class AbstractPoller extends TimerTask{
    final FeedListener<?> listener;

    AbstractPoller(@NonNull FeedListener listener) {
        this.listener = listener;
    }

    /**
     * scan for new entries
     * @param entries the list of entries to scan
     * @return the number of new entries
     */
    @VisibleForTesting
    int scanForNewEntries(@NonNull List<YoutubeVideo> entries) {
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
        val startTime = System.currentTimeMillis();
        if (listener.update()) {
            val latestVideo = listener.getVideos().get(0);

            log.debug("latest entry: {id={}, date={}}", latestVideo.getVideoId(), latestVideo.getPublishDate());

            val entries = scanForNewEntries(listener.getVideos());

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
