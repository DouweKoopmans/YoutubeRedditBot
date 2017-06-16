package com.fallingdutchman.youtuberedditbot.model;

import com.fallingdutchman.youtuberedditbot.YrbUtils;
import com.typesafe.config.Config;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

@Value
public class AppConfig {
    @NonNull History history;
    @NonNull Formatting formatting;
    @NonNull RedditConfig redditConfig;
    @NonNull UserConfig userConfig;
    @NonNull YoutubeConfig youtubeConfig;
    @NonNull ListenerConfig listenerConfig;

    @Value
    public static class History {
        @NonNull String fileExtension;
        @NonNull String folder;

        static History of(@NonNull Config c) {
            val fileExtension = YrbUtils.getPathOrDefault(c, "fileExtension", "json");
            val folder = YrbUtils.getPathOrDefault(c, "folder", "history");

            return new History(fileExtension, folder);
        }
    }

    @Value
    public static class Formatting {
        @NonNull String fileExtension;
        @NonNull String folder;

        static Formatting of(@NonNull Config c) {
            val fileExtension = YrbUtils.getPathOrDefault(c, "fileExtension", "md");
            val folder = YrbUtils.getPathOrDefault(c, "folder", "formats");

            return new Formatting(fileExtension, folder);
        }
    }

    @Value
    public static class RedditConfig {
        @NonNull String appId;
        @NonNull String version;
        /**
         * indicates if we should authorize ourselves with the reddit API.
         *
         * set this to false when testing to prevent being able to post new content to reddit.
         */
        boolean authenticatable;

        static RedditConfig of(@NonNull Config c) {
            val appId = YrbUtils.getPathOrDefault(c, "appId", "com.fallingdutchman.youtuberedditbot");
            val version = YrbUtils.getPathOrDefault(c, "version", "1.0");
            val authenticate = YrbUtils.getPathOrDefault(c, "authenticate", true);

            return new RedditConfig(appId, version, authenticate);
        }
    }

    @Value
    public static class UserConfig {
        @NonNull String folder;
        @NonNull String userConfigFileName;
        @NonNull String appConfigFileName;
        @NonNull String appConfigLocation;
        @NonNull String userConfigLocation;

        static UserConfig of(@NonNull Config c) {
            val userConfigFileName = YrbUtils.getPathOrDefault(c, "userConfigFileName", "bots");
            val folder = YrbUtils.getPathOrDefault(c, "folder", "data");
            val userConfigLocation = c.getString("userConfigLocation");
            val appConfigFileName = YrbUtils.getPathOrDefault(c, "appConfigFileName", "application");
            val appConfigLocation = YrbUtils.getPathOrDefault(c, "appConfigLocation", "application");

            return new UserConfig(folder, userConfigFileName, appConfigFileName, appConfigLocation, userConfigLocation);
        }
    }

    @Value
    public static class YoutubeConfig {
        boolean update;
        @NonNull String applicationName;
        long maxRequestResults;

        static YoutubeConfig of(@NonNull Config c) {
            val update = YrbUtils.getPathOrDefault(c, "update", true);
            val applicationName = YrbUtils.getPathOrDefault(c, "applicationName", "YoutubeRedditBot");
            val maxRequestResults = YrbUtils.getPathOrDefault(c, "maxRequestResults", 20L);

            return new YoutubeConfig(update, applicationName, maxRequestResults);
        }
    }

    @Value
    public static class ListenerConfig {
        int intervalStep;

        static ListenerConfig of(@NonNull Config c) {
            val intervalStep = YrbUtils.getPathOrDefault(c, "intervalStep", 60);

            return new ListenerConfig(intervalStep);
        }
    }

    public static AppConfig of(@NonNull Config c) {
        val history = History.of(c.getConfig("history"));
        val formatting = Formatting.of(c.getConfig("formatting"));
        val reddit = RedditConfig.of(c.getConfig("reddit"));
        val userConfig = UserConfig.of(c.getConfig("userConfig"));
        val youtube = YoutubeConfig.of(c.getConfig("youtube"));
        val listenerConfig = ListenerConfig.of(c.getConfig("listener"));

        return new AppConfig(history, formatting, reddit, userConfig, youtube, listenerConfig);
    }
}
