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

            String type;
            String channelId;
            String youtubeName = null;
            List<String> subreddits;
            boolean postDescription;

            type = instance.getString("type");
            channelId = instance.getString("channelId");
            subreddits = instance.getStringList("subreddit");
            postDescription = instance.getBoolean("postDescription");

            if ("descriptionListener".equals(type)) {
                youtubeName = instance.getString("youtubeName");
            }

            entries.add(createInstance(type, channelId, youtubeName, subreddits,
                    postDescription));
        }
    }

    @VisibleForTesting
    public Instance createInstance(String type, String youtubeFeed, String youtubeName, List<String> subreddits,
                                   boolean postDescription) {
        return new Instance(type, youtubeFeed, youtubeName, subreddits, postDescription);
    }

    public List<Instance> getEntries() {
        return ImmutableList.copyOf(entries);
    }

    public RedditCredentials getRedditCredentials() {
        return redditCredentials;
    }
}
