package com.fallingdutchman.youtuberedditbot.config;

import com.fallingdutchman.youtuberedditbot.config.model.Instance;
import com.fallingdutchman.youtuberedditbot.config.model.RedditCredentials;
import com.google.common.annotations.VisibleForTesting;
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

    private Config conf = ConfigFactory.parseFile(new File("application.conf"));

    private List<Instance> entries = Lists.newArrayList();
    private RedditCredentials redditCredentials;

    private ConfigHandler() {
    }

    // TODO: 20-9-16 instead of a getInstance use dependency injection 
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
            String youtubeFeed;
            String descriptionRegex = null;
            String youtubeName = null;
            List<String> subreddits;
            boolean postDescription;

            type = instance.getString("type");
            youtubeFeed = instance.getString("youtubeFeed");
            subreddits = instance.getStringList("subreddit");
            postDescription = instance.getBoolean("postDescription");

            if ("descriptionListener".equals(type)) {
                youtubeName = instance.getString("youtubeName");


            } else if ("newVideoListener".equals(type)) {
                descriptionRegex = instance.getString("descriptionRegex");
            }

            getEntries().add(createInstance(type, youtubeFeed, descriptionRegex, youtubeName, subreddits,
                    postDescription));
        }
    }

    @VisibleForTesting
    public Instance createInstance(String type, String youtubeFeed, String descriptionRegex, String youtubeName,
                                   List<String> subreddits, boolean postDescription) {
        return new Instance(type, youtubeFeed, descriptionRegex, youtubeName, subreddits, postDescription);
    }

    public List<Instance> getEntries() {
        return entries;
    }

    public RedditCredentials getRedditCredentials() {
        return redditCredentials;
    }
}
