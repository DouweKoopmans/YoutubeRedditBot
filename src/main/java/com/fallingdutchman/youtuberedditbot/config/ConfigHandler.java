package com.fallingdutchman.youtuberedditbot.config;

import com.fallingdutchman.youtuberedditbot.YrbUtils;
import com.fallingdutchman.youtuberedditbot.model.Instance;
import com.fallingdutchman.youtuberedditbot.model.RedditCredentials;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;

import java.io.File;
import java.util.List;

/**
 * Created by Douwe Koopmans on 8-1-16.
 */
public class ConfigHandler {
    private static ConfigHandler ourInstance = new ConfigHandler();

    private final Config conf = ConfigFactory.parseFile(new File(YrbUtils.LOCAL_HOST_FOLDER + "application.conf"));
    private final List<Instance> entries = Lists.newArrayList();
    private RedditCredentials redditCredentials;

    private ConfigHandler() {
    }

    public static ConfigHandler getInstance() {
        return ourInstance;
    }

    public void load(){
        Config rcConfig = conf.getObject("redditCredentials").toConfig();
        redditCredentials = new RedditCredentials(
                rcConfig.getString("redditClientId"),
                rcConfig.getString("redditOauthSecret"),
                rcConfig.getString("redditUsername"),
                rcConfig.getString("redditPassword"));

        List<? extends ConfigObject> instances = conf.getObjectList("instances");
        for (ConfigObject object : instances) {

            Config instance = object.toConfig();

            String pollerType;
            String channelId;
            String youtubeName = null;
            List<String> subreddits;
            boolean postDescription;
            double pollerInterval;
            String youtubeApiKey = null;
            String listenerType;

            if (instance.hasPath("listener-type")) {
                listenerType = instance.getString("listener-type");
            } else {
                listenerType = "rss";
            }

            if (instance.hasPath("poller-type")) {
                pollerType = instance.getString("poller-type");
            } else {
                pollerType = "new-video";
            }

            postDescription = instance.hasPath("post-description") && instance.getBoolean("post-description");

            channelId = instance.getString("channel-id");
            subreddits = instance.getStringList("subreddit");

            if (instance.hasPath("interval")) {
                pollerInterval = instance.getDouble("interval");
            } else {
                pollerInterval = 0.5F;
            }

            if ("api".equals(listenerType)) {
                youtubeApiKey = instance.getString("youtube_api_key");
            }

            if ("descriptionListener".equals(pollerType)) {
                youtubeName = instance.getString("youtubeName");
            }

            entries.add(createInstance(pollerType, channelId, youtubeName, subreddits,
                    postDescription, pollerInterval, youtubeApiKey, listenerType));
        }
    }

    @VisibleForTesting
    public Instance createInstance(String type, String youtubeFeed, String youtubeName, List<String> subreddits,
                                   boolean postDescription, double pollerInterval, String apikey, String listenerType) {
        return new Instance(type, youtubeFeed, youtubeName, subreddits, postDescription, pollerInterval, apikey,
                listenerType);
    }

    public List<Instance> getEntries() {
        return ImmutableList.copyOf(entries);
    }

    public RedditCredentials getRedditCredentials() {
        return redditCredentials;
    }
}
