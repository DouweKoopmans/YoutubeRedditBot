package com.fallingdutchman.youtuberedditbot.listeners;

import com.fallingdutchman.youtuberedditbot.YoutubeVideo;
import com.fallingdutchman.youtuberedditbot.YrbUtils;
import com.google.common.annotations.VisibleForTesting;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.io.FeedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TimerTask;

/**
 * Created by Douwe Koopmans on 10-1-16.
 */
// TODO: 20-9-16 look into how the poller and feedListener interact with each other 
public abstract class AbstractPoller extends TimerTask{
    private static final Logger log = LoggerFactory.getLogger(AbstractPoller.class);
    protected final FeedListener listener;

    protected AbstractPoller(FeedListener listener) {
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
        try {
            listener.updateFeed();
        } catch (IOException e) {
            log.error("was unable to parse the xml", e);
        } catch (FeedException e) {
            log.error("rome was unable to parse the feed", e);
        } catch (IllegalArgumentException e) {
            log.error("rome was unable to understand the feed type", e);
        }

        int entries = scanForNewEntries(listener.getFeed().getEntries());

        if (entries > 0) {
            log.debug(String.format("poller for %s has found %s new videos", listener.getChannelId(), entries));
        }

        this.runPoller(entries);
    }

    protected abstract void runPoller(int entries);

    /**
     * called when a new video has been found and added to the system. creates a new post for this video
     * on reddit.
     * @param video the new video
     * @param subreddit the subreddit this should be posted to
     */
    public void processNewVideo(YoutubeVideo video, String subreddit) {
        listener.authenticator.submitPost(video.getVideoTitle(),
                video.getUrl().toExternalForm(), subreddit);
    }

}
