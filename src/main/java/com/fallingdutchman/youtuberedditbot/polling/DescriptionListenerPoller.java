package com.fallingdutchman.youtuberedditbot.polling;

import com.fallingdutchman.youtuberedditbot.YoutubeFeedListener;
import com.fallingdutchman.youtuberedditbot.YoutubeVideo;
import com.google.common.base.Preconditions;
import com.rometools.rome.feed.synd.SyndEntry;

import java.util.regex.Pattern;

/**
 * Created by Douwe Koopmans on 10-1-16.
 */
public class DescriptionListenerPoller extends AbstractPoller {

    public DescriptionListenerPoller(YoutubeFeedListener listener) {
        super(listener);
    }

    @Override
    protected void runPoller(int entries) {
        for (int i = entries - 1; i >= 0; i--) {
            SyndEntry entry = listener.getFeed().getEntries().get(i);
            YoutubeVideo video = listener.find(entry);
            if (checkEntry(video.getDescription(), listener.getInstance().getYoutubeName(),
                    listener.getInstance().getChannelId())) {
                this.listener.newVideoPosted(video);
            }
        }
    }

    /**
     * checks if the provided description of a video contains a link to our youtuber
     * @param description the description we need to search
     * @param targetName the name of the youtuber we want to find
     * @param targetId the channel id of the youtuber we want to find
     * @return whether a link to the youtuber is present in the description
     */
    static boolean checkEntry(String description, String targetName, String targetId) {
        Preconditions.checkNotNull(description);

        final Pattern regex = Pattern.compile(
                String.format("https?://www\\.youtube\\.com/((user/)?%s|channel/%s)", targetName,
                        targetId), Pattern.UNIX_LINES | Pattern.MULTILINE);

        return regex.matcher(description).find();
    }
}
