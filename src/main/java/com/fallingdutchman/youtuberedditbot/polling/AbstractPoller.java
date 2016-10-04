package com.fallingdutchman.youtuberedditbot.polling;

import com.fallingdutchman.youtuberedditbot.YoutubeFeedListener;
import com.fallingdutchman.youtuberedditbot.YrbUtils;
import com.google.common.annotations.VisibleForTesting;
import com.rometools.rome.feed.synd.SyndEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.TimerTask;

/**
 * Created by Douwe Koopmans on 10-1-16.
 */
public abstract class AbstractPoller extends TimerTask{
    private static final Logger log = LoggerFactory.getLogger(AbstractPoller.class);
    protected final YoutubeFeedListener listener;

    protected AbstractPoller(YoutubeFeedListener listener) {
        this.listener = listener;
    }

    /**
     * scan for new entries
     * @param entries the list of entries to scan
     * @return the number of new entries
     */
    @VisibleForTesting
    int scanForNewEntries(List<SyndEntry> entries) {
        int i = 0;
        for (SyndEntry syndEntry : entries) {
            final LocalDateTime publishDate = YrbUtils.dateToLocalDate(syndEntry.getPublishedDate());
            if (publishDate.isEqual(listener.getLatestVideo())) {
                break; //if we find the "latest date" we know that every entry after that will be even older
            } else if (publishDate.isAfter(listener.getLatestVideo())) {
                i++;
            }
        }
        return i;
    }

    @Override
    public final void run() {
        listener.updateFeed();

        int entries = scanForNewEntries(listener.getFeed().getEntries());

        if (entries > 0) {
            log.debug(String.format("poller for %s has found %s new videos", listener.getChannelId(), entries));
        }

        this.runPoller(entries);
    }

    protected abstract void runPoller(int entries);
}
