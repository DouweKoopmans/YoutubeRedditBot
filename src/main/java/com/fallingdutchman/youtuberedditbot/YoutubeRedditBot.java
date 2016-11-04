package com.fallingdutchman.youtuberedditbot;

import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManager;
import com.fallingdutchman.youtuberedditbot.config.ConfigHandler;
import com.fallingdutchman.youtuberedditbot.listeners.FeedListener;
import com.fallingdutchman.youtuberedditbot.listeners.YoutubeApiListener;
import com.fallingdutchman.youtuberedditbot.listeners.YoutubeRssFeedListener;
import com.fallingdutchman.youtuberedditbot.model.Instance;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Created by Douwe Koopmans on 8-1-16.
 */
@Slf4j
public class YoutubeRedditBot {
    private static FeedRegister feedRegister = new FeedRegister();

    public static void main(String[] args) {
        try {
            new YoutubeRedditBot().run();
        } catch (Exception e) {
            log.error("an unexpected error occurred whilst executing the application. will now exit.", e);
        }
    }

    private void run() {
        log.info("starting up!");

        log.info("applying ShutdownHook");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                log.info("----------------------------------------------------");
                log.info("printing stats");
                feedRegister.print();
                log.info("shutting down!");
            }
        });

        // load config
        try {
            ConfigHandler.getInstance().load();
        } catch (Exception e) {
            log.error("a fatal error occurred whilst trying to load the configurations, exiting", e);
        }

        // get all entries

        log.info("found and initialising {} entries", ConfigHandler.getInstance().getEntries().size());

        ConfigHandler.getInstance().getEntries().forEach(instance -> {
            // add a register a feed listener for every entry
            try {
                log.info("initialising listener for {}", instance);
                final FeedListener<?> feedListener = createFeedListener(instance.getListenerType(), instance,
                        new RedditManager(ConfigHandler.getInstance().getRedditCredentials().getRedditUserName()));
                feedRegister.addEntry(feedListener);
            } catch (IOException e) {
                log.error("was unable to read stream from URL, please make sure the youtubeFeed " +
                        "attribute in your config is correct and you the device is connected to the internet", e);
            } catch (GeneralSecurityException e) {
                log.error("a security exception occurred whilst trying to authenticate with the youtube api", e);
            }
        });

        // start the listener
        log.info("starting listeners");
        feedRegister.getEntries().forEach(FeedListener::listen);
    }

    private static FeedListener<?> createFeedListener(String type, Instance instance, RedditManager manager)
            throws IOException, GeneralSecurityException {
        switch (type) {
            case "api":
                return new YoutubeApiListener(manager, instance);
            case "rss":
            default:
                return YoutubeRssFeedListener.of(instance, manager);
        }
    }
}
