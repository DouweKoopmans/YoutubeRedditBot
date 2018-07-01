package com.fallingdutchman.youtuberedditbot.listeners;

import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManagerRegistry;
import com.fallingdutchman.youtuberedditbot.authentication.twitch.TwitchManager;
import com.fallingdutchman.youtuberedditbot.listeners.filtering.FilterFactory;
import com.fallingdutchman.youtuberedditbot.model.AppConfig;
import com.fallingdutchman.youtuberedditbot.model.Instance;
import com.fallingdutchman.youtuberedditbot.model.Video;
import com.fallingdutchman.youtuberedditbot.processing.ProcessorFactory;
import com.google.common.collect.Streams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Slf4j
@ToString(doNotUseGetters = true, callSuper = true)
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
public class TwitchCollectionsApiListener extends AbstractVideoListener<JsonObject> {

    private final Instance configInstance;
    private final TwitchManager twitchManager;

    @Inject
    public TwitchCollectionsApiListener(@Assisted @NonNull Instance configInstance, ProcessorFactory processorFactory, @NonNull AppConfig config,
                                        RedditManagerRegistry redditManagerRegistry, FilterFactory filterFactory, TwitchManager twitchManager) {
        super(configInstance, processorFactory, config, redditManagerRegistry, filterFactory);
        this.configInstance = configInstance;
        this.twitchManager = twitchManager;
    }

    @Nullable
    @Override
    public Optional<Video> extract(JsonObject target) {
        String videoTitle;
        String videoId;
        URL url;
        LocalDateTime publishDate;
        String description;
        try {
            videoTitle = getJsonElement(target, "title");
            videoId = getJsonElement(target, "item_id");
            url = new URL("https://www.twitch.tv/videos/" + videoId);
            publishDate = LocalDateTime.parse(getJsonElement(target, "published_at"), DateTimeFormatter.ISO_DATE_TIME);
            description = getJsonElement(target, "description_html");

        } catch (MalformedURLException e) {
            log.error("url for twitch video is malformed, check if videoId is correct");
            return Optional.empty();
        } catch (MissingTwitchJsonElementException ex) {
            log.error("the json object obtained from twitch does not contain the right data", ex);
            return Optional.empty();
        }

        return Optional.of(new Video(videoTitle, videoId, url, publishDate, description));
    }

    @Override
    protected Optional<List<JsonObject>> update() {
        try {
            val optionalBase = this.twitchManager.genericTwitchRequest(String.format("collections/%s/items",
                    configInstance.getCollectionTarget()), configInstance.getTwitchClientId());

            return optionalBase.map(baseElement -> Streams.stream(baseElement.getAsJsonArray("items").iterator())
                    .map(JsonElement::getAsJsonObject)
                    .collect(toList()));
        } catch (IOException e) {
            log.error("malformed URL when trying to update the Twitch API for collection ID: "
                    + configInstance.getCollectionTarget(), e);
            return Optional.empty();
        }
    }

    private static String getJsonElement(JsonObject target, String element) {
        if (target.has(element)) {
            return target.getAsJsonPrimitive(element).getAsString();
        } else {
            throw new MissingTwitchJsonElementException(element);
        }
    }
}
