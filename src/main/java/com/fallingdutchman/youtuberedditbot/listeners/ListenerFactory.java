package com.fallingdutchman.youtuberedditbot.listeners;

import com.fallingdutchman.youtuberedditbot.model.Instance;

/**
 * basic factory for every implementation of an YoutubeListener. this factory
 * is automatically implemented by Guice.
 */
public interface ListenerFactory {
    YoutubeRssFeedListener createRss(Instance instance);

    YoutubeApiListener createApi(Instance instance);

    TwitchCollectionsApiListener createTwitchCollections(Instance instance);

    TwitchVideoApiListener createTwitchVideos(Instance instance);
}
