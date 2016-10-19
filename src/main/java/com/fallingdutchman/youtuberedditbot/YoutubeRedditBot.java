package com.fallingdutchman.youtuberedditbot;

import com.fallingdutchman.youtuberedditbot.config.ConfigHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

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
                final YoutubeFeedListener feedListener = YoutubeFeedListener.of(instance,
                        ConfigHandler.getInstance().getRedditCredentials().getRedditUserName());
                feedRegister.addEntry(feedListener);
            } catch (IOException e) {
                log.error("was unable to read stream from URL, please make sure the youtubeFeed " +
                        "attribute in your config is correct and you the device is connected to the internet", e);
            }
        });

        // start the listener
        log.info("starting listeners");
        feedRegister.getEntries().forEach(FeedListener::listen);
    }
}
