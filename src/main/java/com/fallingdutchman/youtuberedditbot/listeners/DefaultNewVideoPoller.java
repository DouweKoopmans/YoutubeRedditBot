package com.fallingdutchman.youtuberedditbot.listeners;

import com.fallingdutchman.youtuberedditbot.YoutubeVideo;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.rometools.rome.feed.synd.SyndEntry;
import net.dean.jraw.http.RestResponse;

/**
 * Created by Douwe Koopmans on 10-1-16.
 */
public class DefaultNewVideoPoller extends AbstractPoller {

    public DefaultNewVideoPoller(FeedListener listener) {
        super(listener);
    }

    @Override
    protected void runPoller(int entries) {
        //this is a reverse loop so it iterates over the videos from oldest to newest, this is needed so the latestvideo
        //object stored in the listener is set to the newest and not the oldest video
        for (int i = entries - 1; i >= 0; i--) {
            SyndEntry entry = listener.getFeed().getEntries().get(i);
            YoutubeVideo video = listener.find(entry);
            this.listener.newVideoPosted(video);
        }
    }

    @Override
    public void processNewVideo(YoutubeVideo video, String subreddit) {
        final RestResponse submitPostResponse = listener.authenticator.submitPost(video.getVideoTitle(),
                video.getUrl().toExternalForm(), subreddit);

        // TODO: 20-9-16 don't use hidden API's
        final JsonElement jsonResponse = new JsonParser().parse(submitPostResponse.getJson().asText());
        String url = jsonResponse.getAsJsonArray()
                .get(18).getAsJsonArray()
                .get(3).getAsJsonArray()
                .get(0)
                .getAsString();

        String cleanedUpUrl = url.replace("http://www.reddit.com/r/" + subreddit + "/comments/", "");
        cleanedUpUrl = cleanedUpUrl.replace(video.getVideoTitle().replace(" ", "_"), "");
        cleanedUpUrl = cleanedUpUrl.replaceAll("/", "");

        listener.authenticator.submitComment(FeedListener.generateMdDescription(video.getDescription()), "t3_"
                + cleanedUpUrl);
    }
}
