package com.fallingdutchman.youtuberedditbot.model;

import com.fallingdutchman.youtuberedditbot.YrbUtils;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import lombok.val;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Value
public class Instance {
    boolean enabled;
    @Nullable String name;
    @Nullable String channelId;
    @NonNull Comment comment;
    @NonNull String pollerType;
    @NonNull RedditCredentials redditCredentials;
    @NonNull List<String> subreddit;
    @Nullable String youtubeApiKey;
    @Nullable Target target;
    double interval;
    @NonNull String listenerType;
    @NonNull String type;
    @Nullable String twitchClientId;
    @Nullable String collectionTarget;

    @Value
    public static class Target {
        @NonNull String youtubeName;
        @NonNull String channelId;

        static Target of(Config c) {
            val youtubeName = c.getString("youtubeName");
            val channelId = c.getString("channelId");

            return new Target(youtubeName, channelId);
        }
    }

    @Value
    public static class Comment {
        @NonNull String formatPath;
        boolean postComment;
        @NonNull List<CommentRule> rules;

        @Value
        public static class CommentRule {
            @NonNull String find;
            @NonNull String replace;

            static CommentRule of(Config c) {
                val find = c.getString("find");
                val replace = c.getString("replace");

                return new CommentRule(find, replace);
            }

            @Override
            public String toString() {
                return "CommentRule{" +
                        "find='" + find.replaceAll("\n", "\\\\n") + '\'' +
                        ", replace='" + replace.replaceAll("\n", "\\\\n") + '\'' +
                        '}';
            }
        }

        static Comment of(Config c) {
            val formatPath = c.getString("formatPath");
            val postComment = c.hasPathOrNull("postComment") && c.getBoolean("postComment");
            List<CommentRule> rules = Collections.unmodifiableList(c.getList("rules").stream()
                    .map(cv -> Comment.CommentRule.of(((ConfigObject) cv).toConfig()))
                    .collect(Collectors.toList()));

            return new Comment(formatPath, postComment, rules);
        }

    }

    @Value
    @ToString(of = {"redditUsername", "redditClientId"})
    public static class RedditCredentials {
        @NonNull String redditClientId;
        @NonNull String redditOauthSecret;
        @NonNull String redditPassword;
        @NonNull String redditUsername;

        static RedditCredentials of(Config c) {
            val redditClientId = c.getString("redditClientId");
            val redditOauthSecret = c.getString("redditOauthSecret");
            val redditPassword = c.getString("redditPassword");
            val redditUsername = c.getString("redditUsername");

            return new RedditCredentials(redditClientId, redditOauthSecret, redditPassword, redditUsername);
        }
    }

    public static Instance of(Config c) {
        val enabled = c.getBoolean("enabled");
        val name = c.getString("name");
        val channelId = YrbUtils.getPathOrDefault(c, "channelId", null);
        val comment = Comment.of(c.getConfig("comment"));
        val pollerType = YrbUtils.getPathOrDefault(c, "pollerType", "new-video");
        val redditCredentials = RedditCredentials.of(c.getConfig("redditCredentials"));
        List<String> subreddit = Collections.unmodifiableList(c.getList("subreddit").stream()
                .map(cv -> String.valueOf(cv.unwrapped()))
                .collect(Collectors.toList()));
        val interval = YrbUtils.getPathOrDefault(c, "interval", 5D);
        val listenerType = YrbUtils.getPathOrDefault(c, "listenerType", "rss");
        String youtubeApiKey = YrbUtils.getPathOrDefault(c, "youtubeApiKey", null);
        val target = YrbUtils.getOptionalConfig(c, "target").map(Target::of).orElse(null);
        val twitchClientApi = YrbUtils.getPathOrDefault(c, "twitch-client-id", null);
        val collectionTarget = YrbUtils.getPathOrDefault(c, "collectionTarget", null);
        val type = YrbUtils.getPathOrDefault(c, "type", "youtube");

        val instance = new Instance(enabled, name, channelId, comment, pollerType, redditCredentials, subreddit, youtubeApiKey,
                target, interval, listenerType, type, twitchClientApi, collectionTarget);

        return sanityCheck(instance);
    }

    /**
     * checks if the conditionally required parameters of instance are present.
     * @param instance the instance which should be checked
     * @return the instance that was referenced
     * @throws IllegalArgumentException if a required element of the instance is missing
     */
    private static Instance sanityCheck(final Instance instance) throws IllegalArgumentException {
        if (instance.getType().equalsIgnoreCase("youtube")) {
            if (instance.getChannelId() == null) {
                throw new IllegalArgumentException("channelId can not be null for type \"youtube\"");
            }
            if (instance.getChannelId().isEmpty()) {
                throw new IllegalArgumentException("channelId can not be empty for type \"youtube\"");
            }

            if (instance.getListenerType().equalsIgnoreCase("api")) {
                if (instance.getYoutubeApiKey() == null) {
                    throw new IllegalArgumentException("youtube-api-key can not be null for type \"youtube\"");
                }
                if (instance.getYoutubeApiKey().isEmpty()) {
                    throw new IllegalArgumentException("youtube-api-key can not be empty for type \"youtube\"");
                }
            }

            if (instance.getPollerType().equalsIgnoreCase("description-mention") && instance.getTarget() == null) {
                throw new IllegalArgumentException("target can not be null for description-mention poller for type " +
                        "\"youtube\"");
            }
        } else if (instance.getType().equalsIgnoreCase("twitch")) {
            if (instance.getTwitchClientId() == null) {
                throw new IllegalArgumentException("twitch-client-id can not be null for type \"twitch\"");
            }
            if (instance.getTwitchClientId().isEmpty()) {
                throw new IllegalArgumentException("twitch-client-id can not be empty for type \"twitch\"");
            }

            if (instance.getCollectionTarget() == null) {
                throw new IllegalArgumentException("collectionTarget can not be null for type \"twitch\"");
            }

            if (instance.getCollectionTarget().isEmpty()) {
                throw new IllegalArgumentException("collectionTarget can not be empty for type \"twitch\"");
            }
        } else {
            throw new IllegalArgumentException(String.format("\"%s\" is not a valid type", instance.getType()));
        }

        return instance;
    }
}
      
