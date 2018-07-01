package com.fallingdutchman.youtuberedditbot;

import com.fallingdutchman.youtuberedditbot.listeners.AbstractVideoListener;
import com.fallingdutchman.youtuberedditbot.listeners.ListenerFactory;
import com.fallingdutchman.youtuberedditbot.model.Instance;
import com.google.api.client.util.Lists;
import com.google.inject.Guice;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;

/**
 * Created by Douwe Koopmans on 8-1-16.
 */
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class YoutubeRedditBot {
    final List<AbstractVideoListener> listeners = Lists.newArrayList();
    final ConfigManager configManager;
    ListenerFactory listenerFactory;

    private YoutubeRedditBot() {
        configManager = new ConfigManager();
    }

    public static void main(String[] args) {
        try {
            new YoutubeRedditBot().run();
        } catch (Exception e) {
            log.error("an unexpected error occurred whilst executing the application. will now exit.", e);
        }
    }

    private void run() {
        log.info("");
        log.info("#################################");
        log.info("starting up!");
        log.info("#################################");
        log.info("");
        val injector = Guice.createInjector(new YrbModule(configManager.getAppConfig()));

        listenerFactory = injector.getInstance(ListenerFactory.class);

        val instances = configManager.getInstances();

        if (!configManager.getAppConfig().getYoutubeConfig().isUpdate()) {
            log.warn("not updating with youtube API");
        }

        if (!configManager.getAppConfig().getRedditConfig().isAuthenticatable()) {
            log.warn("not authenticating with reddit API");
        }

        log.info("found and initialising {} entries", instances.size());

        // create listeners
        instances.forEach(instance -> {
            if (!instance.isEnabled()) {
                log.warn("instance is disabled. Instance: " + instance.getName());
                return;
            }

            log.info("initialising listener for {}", instance);
            val feedListener = createFeedListener(instance.getType(), instance.getListenerType(), instance);
            listeners.add(feedListener);
        });

        // start listeners
        log.info("starting listeners");
        listeners.forEach(listener -> {
            try {
                listener.listen();
            } catch (Exception e) {
                // listener couldn't be started
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("shutting down!");
            log.info("----------------------------------------------------");
            log.info("there are currently {} feeds listening, feeds:", listeners.size());
            listeners.forEach(listener -> {
                log.info(listener.toString());
                listener.stopListening();
            });
            log.info("----------------------------------------------------");
        }));
    }

    @NonNull
    private AbstractVideoListener<?> createFeedListener(String type, String listenerType, Instance instance) {
        if (type.equalsIgnoreCase("twitchCollection")) {
            return listenerFactory.createTwitchCollections(instance);
        } else if (type.equalsIgnoreCase("twitchVideo")) {
            return listenerFactory.createTwitchVideos(instance);
        }

        switch (listenerType) {
            case "api":
                return listenerFactory.createApi(instance);
            case "rss":
            default:
                return listenerFactory.createRss(instance);
        }
    }
}
