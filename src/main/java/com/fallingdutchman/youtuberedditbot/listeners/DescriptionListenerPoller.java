package com.fallingdutchman.youtuberedditbot.listeners;

import com.fallingdutchman.youtuberedditbot.YoutubeVideo;
import com.rometools.rome.feed.synd.SyndEntry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Douwe Koopmans on 10-1-16.
 */
public class DescriptionListenerPoller extends AbstractPoller {

    protected DescriptionListenerPoller(FeedListener listener) {
        super(listener);
    }

    @Override
    protected void runPoller(int entries) {
        for (int i = entries - 1; i >= 0; i--) {
            SyndEntry entry = listener.getFeed().getEntries().get(i);
            YoutubeVideo video = listener.find(entry);
            Matcher matcher = Pattern.compile("https?://www\\.youtube\\.com/" +
                    listener.getConfigInstance().getYoutubeName()).matcher(video.getDescription());

            if (matcher.find()) {
                this.listener.newVideoPosted(video);
            }
        }
    }
}
