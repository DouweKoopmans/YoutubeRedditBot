package com.fallingdutchman.youtuberedditbot.authentication.youtube;

import com.fallingdutchman.youtuberedditbot.model.AppConfig;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.Video;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Synchronized;
import lombok.experimental.FieldDefaults;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;

/**
 * Created by douwe on 3-7-17.
 */
@Singleton
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class YoutubeManager {
    private final AppConfig.YoutubeConfig config;
    YouTube youTube;

    @Inject
    public YoutubeManager(@NonNull AppConfig config) throws GeneralSecurityException, IOException {

        this.config = config.getYoutubeConfig();
        this.youTube = new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(), request -> {
            // no-op
        }).setApplicationName(this.config.getApplicationName()).build();
    }

    @Synchronized
    public YouTube getYouTube() {
        return youTube;
    }

    public List<PlaylistItem> getVideosFromPlaylist(final String playlistId, final String apiKey) throws IOException {
        testApiKey(apiKey);

        return this.getYouTube()
                .playlistItems()
                .list("snippet")
                .setPlaylistId(playlistId)
                .setFields("items(snippet/publishedAt, snippet/title, snippet/description, snippet/resourceId/videoId)")
                .setMaxResults(config.getMaxRequestResults())
                .setKey(apiKey)
                .execute()
                .getItems();
    }

    public List<Channel> getChannelFromId(final String channelId, final String apiKey) throws IOException {
        testApiKey(apiKey);

        return this.getYouTube()
                .channels()
                .list("contentDetails")
                .setId(channelId)
                .setFields("items/contentDetails")
                .setKey(apiKey)
                .setMaxResults(1L)
                .execute()
                .getItems();
    }

    public Optional<Video> getVideoDataFromVideoId(final String videoId, final String apiKey) throws IOException {
        testApiKey(apiKey);

        return this.getYouTube()
                .videos()
                .list("snippet")
                .setFields("items(snippet/title)")
                .setId(videoId)
                .setMaxResults(1L)
                .setKey(apiKey)
                .execute()
                .getItems()
                .stream()
                .findFirst();
    }

    private void testApiKey(final String apiKey) {
        Preconditions.checkNotNull(apiKey, "need a youtube api key to run an API listener");
        Preconditions.checkArgument(!apiKey.isEmpty(), "need a youtube api key to run an API listener");
    }
}
