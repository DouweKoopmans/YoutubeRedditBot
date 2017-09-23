package com.fallingdutchman.youtuberedditbot.listeners;

import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManagerRegistry;
import com.fallingdutchman.youtuberedditbot.history.HistoryManager;
import com.fallingdutchman.youtuberedditbot.listeners.filtering.FilterFactory;
import com.fallingdutchman.youtuberedditbot.model.AppConfig;
import com.fallingdutchman.youtuberedditbot.model.Instance;
import com.fallingdutchman.youtuberedditbot.model.Video;
import com.fallingdutchman.youtuberedditbot.processing.ProcessorFactory;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@Slf4j
@ToString(doNotUseGetters = true, callSuper = true)
public class TwitchApiListener extends AbstractVideoListener<JsonObject> {

    private final Instance configInstance;

    @Inject
    public TwitchApiListener(@Assisted @NonNull Instance configInstance, ProcessorFactory processorFactory, @NonNull AppConfig config,
                             RedditManagerRegistry redditManagerRegistry, FilterFactory filterFactory,
                             HistoryManager historyManager) {
        super(configInstance, processorFactory, config, redditManagerRegistry, filterFactory, historyManager);
        this.configInstance = configInstance;
    }

    @Nullable
    @Override
    public Video extract(JsonObject target) {
        String videoTitle;
        String videoId;
        URL url;
        LocalDateTime publishDate;
        String description;
        try {

            if (target.has("title")) {
                videoTitle = target.get("title").getAsString();
            } else {
                throw new RuntimeException("title element is missing");
            }

            if (target.has("item_id")) {
                videoId = target.get("item_id").getAsString();
            } else {
                throw new RuntimeException("item_id element is missing");
            }

            url = new URL("https://www.twitch.tv/videos/" + videoId);

            if (target.has("published_at")) {
                publishDate = LocalDateTime.parse(target.get("published_at").getAsString(),
                        DateTimeFormatter.ISO_DATE_TIME);
            } else {
                throw new RuntimeException("published_at element is missing");
            }

            if (target.has("description_html")) {
                description = target.get("description_html").getAsString();
            } else {
                throw new RuntimeException("description_html element is missing");
            }
        } catch (MalformedURLException e) {
            log.error("url for twitch video is malformed");
            return null;
        } catch (RuntimeException ex) {
            log.error("the json object obtained from twitch does not contain the right data", ex);
            return null;
        }

        return new Video(videoTitle, videoId, url, publishDate, description);
    }

    @Override
    protected boolean update() {
        try {
            val jsonParser= new JsonParser();
            val url = String.format("https://api.twitch.tv/kraken/collections/%s/items",
                    configInstance.getCollectionTarget());
            val in = (HttpURLConnection) (new URL(url).openConnection());
            in.setRequestProperty("accept", "application/vnd.twitchtv.v5+json");
            in.setRequestProperty("Client-ID", configInstance.getTwitchClientId());
            in.setRequestMethod("GET");
            in.setDoOutput(true);


            JsonObject baseElement;
            if (in.getResponseCode() != 200) {
                baseElement = jsonParser.parse(new InputStreamReader(in.getErrorStream())).getAsJsonObject();
                log.error(baseElement.toString());
                return false;

            } else {
                baseElement = jsonParser.parse(new InputStreamReader(in.getInputStream())).getAsJsonObject();
            }

            setVideos(StreamSupport.stream(baseElement.getAsJsonArray("items").spliterator(), false)
                    .map(JsonElement::getAsJsonObject)
                    .map(this::extract)
                    .filter(Objects::nonNull)
                    .sorted()
                    .collect(toList()));

            return true;
        } catch (IOException e) {
            log.error("malformed URL when trying to update the Twitch API for collection ID: "
                    + configInstance.getCollectionTarget(), e);
            return false;
        }
    }
}
