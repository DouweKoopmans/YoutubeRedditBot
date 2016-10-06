package com.fallingdutchman.youtuberedditbot;

import com.fallingdutchman.youtuberedditbot.config.ConfigHandler;
import com.fallingdutchman.youtuberedditbot.model.Instance;
import com.rometools.rome.io.FeedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by Douwe Koopmans on 8-1-16.
 */
public class YoutubeRedditBot {
    private static final Logger log = LoggerFactory.getLogger(YoutubeRedditBot.class);
    private static FeedRegister feedRegister = new FeedRegister();

    // TODO: 12-1-16 add dry-run
    // TODO: 29-9-16 provide password and username through command-line, not config file 
    public static void main(String[] args) {
        try {
            new YoutubeRedditBot().run();
        } catch (Exception e) {
            log.error("an unexpected error occurred whilst executing the application. will now exit the application." +
                    " the stacktrace", e);
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
        List<Instance> entries = ConfigHandler.getInstance().getEntries();

        log.info(String.format("found and initialising %s entries", entries.size()));

        entries.forEach(instance -> {
            // add a register a feed listener for every entry
            try {
                feedRegister.addEntry(YoutubeFeedListener.of(instance,
                        ConfigHandler.getInstance().getRedditCredentials().getRedditUserName()));
            } catch (IOException e) {
                log.error("was unable to read stream from URL, please make sure the youtubeFeed " +
                        "attribute in your config is correct and you the device is connected to the internet", e);
            } catch (FeedException e) {
                log.error("was unable to parse feed, please make sure the youtubeFeed attribute" +
                        "in your config is correct", e);
            }
        });

        // start the listener
        feedRegister.getEntries().forEach(IFeedListener::listen);
    }
}
