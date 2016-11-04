package com.fallingdutchman.youtuberedditbot.polling;

import com.fallingdutchman.youtuberedditbot.YoutubeVideo;
import com.fallingdutchman.youtuberedditbot.listeners.FeedListener;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * Created by Douwe Koopmans on 10-1-16.
 */
@Slf4j
public class DescriptionListenerPoller extends AbstractPoller {

    public DescriptionListenerPoller(FeedListener listener) {
        super(listener);
    }

    @Override
    protected void runPoller(int entries) {
        for (int i = entries - 1; i >= 0; i--) {
            YoutubeVideo video = listener.getVideos().get(i);
            if (checkEntry(video.getDescription(), listener.getInstance().getYoutubeName(),
                    listener.getInstance().getChannelId())) {
                this.listener.newVideoPosted(video);
            } else {
                log.debug("was unable to extract video from rss feed, feed might have been malformed in some way");
            }
        }
    }

    /**
     * checks if the provided description of a video contains a link to our youtuber
     * @param description the description we need to search
     * @param targetName the name of the youtuber we want to extract
     * @param targetId the channel id of the youtuber we want to extract
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
