package com.fallingdutchman.youtuberedditbot.polling;

import com.fallingdutchman.youtuberedditbot.YoutubeFeedListener;
import com.fallingdutchman.youtuberedditbot.YoutubeVideo;
import com.rometools.rome.feed.synd.SyndEntry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Douwe Koopmans on 10-1-16.
 */
// TODO: 2-10-16 test this
public class DescriptionListenerPoller extends AbstractPoller {

    public DescriptionListenerPoller(YoutubeFeedListener listener) {
        super(listener);
    }

    @Override
    protected void runPoller(int entries) {
        for (int i = entries - 1; i >= 0; i--) {
            SyndEntry entry = listener.getFeed().getEntries().get(i);
            YoutubeVideo video = listener.find(entry);
            Matcher matcher = Pattern.compile("https?://www\\.youtube\\.com/" +
                    listener.getInstance().getYoutubeName()).matcher(video.getDescription());

            if (matcher.find()) {
                this.listener.newVideoPosted(video);
            }
        }
    }
}
