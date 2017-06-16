package com.fallingdutchman.youtuberedditbot;

import com.fallingdutchman.youtuberedditbot.listeners.AbstractYoutubeListener;
import com.fallingdutchman.youtuberedditbot.listeners.YoutubeListenerFactory;
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
    final List<AbstractYoutubeListener> listeners = Lists.newArrayList();
    final ConfigManager configManager;
    YoutubeListenerFactory listenerFactory;

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

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("shutting down!");
            log.info("----------------------------------------------------");
            log.info("there are currently {} feeds listening, feeds:", listeners.size());
            for (int i = 0; i < listeners.size(); i++) {
                val listener = listeners.get(i);

                log.info("feed {}: ", i + 1);
                log.info(listener.toString());
                listener.stopListening();
            }
            log.info("----------------------------------------------------");
        }));

        listenerFactory = injector.getInstance(YoutubeListenerFactory.class);

        val instances = configManager.loadInstances();

        log.info("found and initialising {} entries", instances.size());

        // create listeners
        instances.forEach(instance -> {
            log.info("initialising listener for {}", instance);
            val feedListener = createFeedListener(instance.getListenerType(), instance);
            listeners.add(feedListener);
        });

        // start listeners
        log.info("starting listeners");
        listeners.forEach(AbstractYoutubeListener::listen);
    }

    @NonNull
    private AbstractYoutubeListener<?> createFeedListener(String type, Instance instance) {
        switch (type) {
            case "api":
                return listenerFactory.createApi(instance);
            case "rss":
            default:
                return listenerFactory.createRss(instance);
        }
    }
}
