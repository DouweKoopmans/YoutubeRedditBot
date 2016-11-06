package com.fallingdutchman.youtuberedditbot.config;

import com.fallingdutchman.youtuberedditbot.YrbUtils;
import com.fallingdutchman.youtuberedditbot.model.CommentRule;
import com.fallingdutchman.youtuberedditbot.model.Instance;
import com.fallingdutchman.youtuberedditbot.model.RedditCredentials;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import lombok.val;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

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
        val rcConfig = conf.getObject("redditCredentials").toConfig();
        redditCredentials = new RedditCredentials(
                rcConfig.getString("redditClientId"),
                rcConfig.getString("redditOauthSecret"),
                rcConfig.getString("redditUsername"),
                rcConfig.getString("redditPassword"));

        conf.getObjectList("instances").stream()
                .map(ConfigObject::toConfig)
                .map(this::extractInstance)
                .forEach(entries::add);
    }

    private Instance extractInstance(final Config instanceConfig) {
        String pollerType = "new-video";
        String channelId;
        String youtubeName = null;
        List<String> subreddits = Lists.newArrayList();
        boolean postComment = false;
        double pollerInterval = 0.5F;
        String youtubeApiKey = null;
        String listenerType = "rss";
        String commentFormatPath = null;
        List<CommentRule> commentRules = Lists.newArrayList();

        if (instanceConfig.hasPath("listener-type")) {
            listenerType = instanceConfig.getString("listener-type");
        }

        if (instanceConfig.hasPath("poller-type")) {
            pollerType = instanceConfig.getString("poller-type");
        }

        channelId = instanceConfig.getString("channel-id");

        if (instanceConfig.hasPath("subreddit")) {
            subreddits = instanceConfig.getStringList("subreddit");
        }

        if (instanceConfig.hasPath("interval")) {
            pollerInterval = instanceConfig.getDouble("interval");
        }

        if ("api".equals(listenerType)) {
            youtubeApiKey = instanceConfig.getString("youtube_api_key");
        }

        if ("descriptionListener".equals(pollerType)) {
            youtubeName = instanceConfig.getString("youtubeName");
        }

        if (instanceConfig.hasPath("comment") && instanceConfig.getObject("comment").toConfig()
                .hasPath("post-comment")) {
            val commentConfig = instanceConfig.getObject("comment").toConfig();

            postComment = commentConfig.getBoolean("post-comment");
            commentFormatPath = commentConfig.getString("format-path");

            if (commentConfig.hasPath("rules")) {
                commentRules = commentConfig.getObjectList("rules").stream()
                        .map(ConfigObject::toConfig)
                        .map(config -> new CommentRule(config.getString("find"), config.getString("replace")))
                        .collect(Collectors.toList());
            }
        }
        return new Instance(youtubeName, pollerType, channelId, subreddits, postComment, pollerInterval, youtubeApiKey,
                listenerType, commentRules, commentFormatPath);
    }

    public List<Instance> getEntries() {
        return ImmutableList.copyOf(entries);
    }

    public RedditCredentials getRedditCredentials() {
        return redditCredentials;
    }
}
