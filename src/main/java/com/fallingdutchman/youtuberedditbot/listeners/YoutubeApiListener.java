package com.fallingdutchman.youtuberedditbot.listeners;

import com.fallingdutchman.youtuberedditbot.YrbUtils;
import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManagerRegistry;
import com.fallingdutchman.youtuberedditbot.history.HistoryManager;
import com.fallingdutchman.youtuberedditbot.listeners.filtering.FilterFactory;
import com.fallingdutchman.youtuberedditbot.model.AppConfig;
import com.fallingdutchman.youtuberedditbot.model.Instance;
import com.fallingdutchman.youtuberedditbot.model.YoutubeVideo;
import com.fallingdutchman.youtuberedditbot.processing.ProcessorFactory;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Lists;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

/**
 * Created by douwe on 19-10-16.
 */
@Slf4j
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class YoutubeApiListener extends AbstractYoutubeListener<PlaylistItem> {
    YouTube youtube;
    AppConfig.YoutubeConfig youtubeConfig;

    @Inject
    public YoutubeApiListener(@Assisted @NonNull Instance instance, ProcessorFactory processorFactory,
                              @NonNull AppConfig config, RedditManagerRegistry redditRegistry, FilterFactory filterFactory,
                              HistoryManager historyManager)
            throws IOException, GeneralSecurityException {
        super(instance, processorFactory, config, redditRegistry, filterFactory, historyManager);

        Preconditions.checkNotNull(instance.getYoutubeApiKey(), "need a youtube api key to run an API listener");
        Preconditions.checkArgument(!instance.getYoutubeApiKey().isEmpty(), "need a youtube api key to run an API listener");

        youtubeConfig = config.getYoutubeConfig();
        youtube = new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(), request -> {
            // no-op
        }).setApplicationName(youtubeConfig.getApplicationName()).build();
    }

    @Synchronized
    @Override
    public boolean update() {
        if (!config.getYoutubeConfig().isUpdate())
            return true;

        try {
            List<Channel> channelsList = youtube.channels()
                    .list("contentDetails")
                    .setId(getInstance().getChannelId())
                    .setFields("items/contentDetails")
                    .setKey(getInstance().getYoutubeApiKey())
                    .execute()
                    .getItems();

            if (channelsList != null && !channelsList.isEmpty()) {
                val uploadPlaylistId = channelsList.get(0).getContentDetails().getRelatedPlaylists().getUploads();

                List<PlaylistItem> result = Lists.newArrayList();

                result.addAll(youtube.playlistItems().list("snippet")
                        .setPlaylistId(uploadPlaylistId)
                        .setFields("items(snippet/publishedAt, snippet/title, snippet/description, snippet/resourceId/videoId)")
                        .setMaxResults(youtubeConfig.getMaxRequestResults())
                        .setKey(getInstance().getYoutubeApiKey())
                        .execute()
                        .getItems());

                setVideos(result.stream()
                        .map(this::extract)
                        .filter(Objects::nonNull)
                        .sorted()
                        .collect(toList()));
                result.clear();
                return true;
            } else {
                log.warn("was unable to find any channels by this id ({}), please make sure this is correct",
                        getInstance().getChannelId());
                return false;
            }
        } catch (IOException e) {
            log.error("an error occurred polling the youtube api", e);
            return false;
        }
    }

    @Override
    public YoutubeVideo extract(@NonNull final PlaylistItem target) {
        URL url;
        try {
            url = new URL("https://www.youtube.com/watch?v=" + target.getSnippet().getResourceId().getVideoId());
        } catch (MalformedURLException e) {
            log.error("url on target entry is malformed", e);
            return null;
        }

        val description = target.getSnippet().getDescription();
        val videoId = target.getSnippet().getResourceId().getVideoId();
        val videoTitle = target.getSnippet().getTitle();
        val publishDate = YrbUtils.dateTimeToLocalDateTime(target.getSnippet().getPublishedAt());

        return new YoutubeVideo(videoTitle, videoId, url, publishDate, description);
    }
}
