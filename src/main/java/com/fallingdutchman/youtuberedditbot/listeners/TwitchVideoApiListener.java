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
import com.google.inject.assistedinject.Assisted;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.annotation.Nullable;
import javax.inject.Inject;
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
public class TwitchVideoApiListener extends AbstractVideoListener<JsonObject> {
    private final Instance configInstance;
    private final TwitchManager twitchManager;

    @Inject
    public TwitchVideoApiListener(@Assisted @NonNull Instance configInstance, ProcessorFactory processorFactory,
                                  @NonNull AppConfig config, RedditManagerRegistry redditManagerRegistry,
                                  FilterFactory filterFactory, TwitchManager twitchManager) {
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
            videoId = getJsonElement(target, "id");
            url = new URL(getJsonElement(target, "url"));
            publishDate = LocalDateTime.parse(getJsonElement(target, "published_at"), DateTimeFormatter.ISO_DATE_TIME);
            description = getJsonElement(target, "description");

        } catch (MalformedURLException e) {
            log.error("url for twitch video is malformed");
            return Optional.empty();
        } catch (MissingTwitchJsonElementException ex) {
            log.error("the json object obtained from twitch does not contain the right data", ex);
            return Optional.empty();
        }

        return Optional.of(new Video(videoTitle, videoId, url, publishDate, description));
    }

    @Override
    protected Optional<List<JsonObject>> update() {

        val channelId = getChannelIdFromName(configInstance.getChannelTarget());
        if (!channelId.isPresent()) {
            return Optional.empty();
        }

        try {
            final Optional<JsonObject> jsonResponse =
                    twitchManager.genericTwitchRequest("videos?user_id=" + channelId.get(), configInstance.getTwitchClientId());

            return jsonResponse
                    .filter(responseJson -> !responseJson.has("error"))
                    .map(baseObject -> Streams.stream(baseObject.getAsJsonArray("data").iterator())
                            .map(JsonElement::getAsJsonObject)
                            .collect(toList()));
        } catch (MalformedURLException e) {
            log.error("malformed URL when trying to update the Twitch API for collection ID: "
                    + configInstance.getCollectionTarget(), e);
            return Optional.empty();
        }
    }

    private Optional<String> getChannelIdFromName(String name) {
        try {
            val optionalBase = twitchManager.genericTwitchRequest("users?login=" + name, configInstance.getTwitchClientId());

            if (optionalBase.isPresent()) {
                final JsonObject baseElement = optionalBase.get();
                if (!baseElement.has("error")) {
                    final JsonObject user = baseElement.getAsJsonArray("data").get(0).getAsJsonObject();

                    if (user.has("id")) {
                        return Optional.of(user.getAsJsonPrimitive("id").getAsString());
                    } else {
                        log.error("unable to retrieve channel id, response is missing id field");
                        return Optional.empty();
                    }
                } else {
                    log.error("received an error from twitch trying to get the channel id for " + name);
                    val status = baseElement.getAsJsonPrimitive("status").getAsInt();

                    if (status == 500) {
                        log.error("received 500 code, twitch is unavailable");
                    } else if (status == 503) {
                        log.error("received 503, twitch might be having issues");
                    } else {
                        log.error("{}: {}, \"{}\"", status, baseElement.getAsJsonPrimitive("error"),
                                baseElement.getAsJsonPrimitive("message"));
                    }
                }
            }

            return Optional.empty();
        } catch (IOException e) {
            log.error(String.format("malformed URL when trying to get the channel id for %s", configInstance.getChannelTarget()));
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
