package com.fallingdutchman.youtuberedditbot.model;

import com.fallingdutchman.youtuberedditbot.YrbUtils;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import lombok.*;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Value
public class Instance {
    @NonNull String channelId;
    @NonNull Comment comment;
    @NonNull String pollerType;
    @NonNull RedditCredentials redditCredentials;
    @NonNull List<String> subreddit;
    @Nullable String youtubeApiKey;
    @Nullable Target target;
    double interval;
    @NonNull String listenerType;

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
        val channelId = c.getString("channelId");
        val comment = Comment.of(c.getConfig("comment"));
        val pollerType = YrbUtils.getPathOrDefault(c, "pollerType", "new-video");
        val redditCredentials = RedditCredentials.of(c.getConfig("redditCredentials"));
        List<String> subreddit = Collections.unmodifiableList(c.getList("subreddit").stream()
                .map(cv -> String.valueOf(cv.unwrapped()))
                .collect(Collectors.toList()));
        val interval = YrbUtils.getPathOrDefault(c, "interval", 0.5D);
        val listenerType = YrbUtils.getPathOrDefault(c, "listenerType", "rss");
        String youtubeApiKey = YrbUtils.getPathOrDefault(c, "youtubeApiKey", null);
        val target = YrbUtils.getOptionalConfig(c, "target").map(Target::of).orElse(null);

        return new Instance(channelId, comment, pollerType, redditCredentials, subreddit,youtubeApiKey, target,
                interval, listenerType);
    }
}
      
