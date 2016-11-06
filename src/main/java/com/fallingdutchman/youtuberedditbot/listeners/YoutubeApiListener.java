package com.fallingdutchman.youtuberedditbot.listeners;

import com.fallingdutchman.youtuberedditbot.YrbUtils;
import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManager;
import com.fallingdutchman.youtuberedditbot.model.Instance;
import com.fallingdutchman.youtuberedditbot.model.YoutubeVideo;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Lists;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by douwe on 19-10-16.
 */
@Slf4j
@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class YoutubeApiListener extends AbstractYoutubeListener<PlaylistItem> {
    YouTube youtube;
    List<Optional<YoutubeVideo>> videos = Lists.newArrayList();

    public YoutubeApiListener(RedditManager authenticator, Instance instance) throws IOException,
            GeneralSecurityException {
        super(authenticator, instance);

        Preconditions.checkNotNull(instance.getApiKey());
        Preconditions.checkArgument(!instance.getApiKey().isEmpty());

        youtube = new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(), request -> {
            // no-op
        }).setApplicationName("YoutubeRedditBot").build();
    }

    @Override
    public boolean update() {
        List<PlaylistItem> result = Lists.newArrayList();
        try {
            YouTube.Channels.List channelRequest = youtube.channels().list("contentDetails")
                    .setId(getInstance().getChannelId())
                    .setFields("items/contentDetails")
                    .setKey(getInstance().getApiKey());
            ChannelListResponse channelResult = channelRequest.execute();
            List<Channel> channelsList = channelResult.getItems();

            if (channelsList != null && !channelsList.isEmpty()) {
                val uploadPlaylistId = channelsList.get(0).getContentDetails().getRelatedPlaylists().getUploads();

                val playlistItemRequest = youtube.playlistItems().list("snippet");
                playlistItemRequest
                        .setPlaylistId(uploadPlaylistId)
                        .setFields(
                                "items(snippet/publishedAt, snippet/title, snippet/description," +
                                        " snippet/resourceId/videoId)")
                        .setMaxResults(15L);
                playlistItemRequest.setKey(getInstance().getApiKey());
                result.addAll(playlistItemRequest.execute().getItems());
            }
        } catch (IOException e) {
            log.error("an error occurred polling the youtube api", e);
            return false;
        }

        videos.clear();
        videos.addAll(result.stream()
                .map(this::extract)
                .sorted(YrbUtils.getOptionalVideoComparator())
                .collect(Collectors.toList()));
        return true;
    }

    @Override
    public List<YoutubeVideo> getVideos() {
        return ImmutableList.copyOf(videos.stream()
                .map(video -> {
                    if (video.isPresent()) {
                        return video.get();
                    } else {
                        throw new IllegalStateException("there are malformed entries in the feed");
                    }
                })
                .sorted()
                .collect(Collectors.toList()));
    }

    @Override
    public Optional<YoutubeVideo> extract(@NonNull final PlaylistItem target) {
        URL url;
        try {
            url = new URL("https://www.youtube.com/watch?v=" + target.getSnippet().getResourceId().getVideoId());
        } catch (MalformedURLException e) {
            log.error("url on target entry is malformed", e);
            return Optional.empty();
        }

        val description = target.getSnippet().getDescription();
        val videoId = target.getSnippet().getResourceId().getVideoId();
        val videoTitle = target.getSnippet().getTitle();
        val publishDate = YrbUtils.dateTimeToLocalDateTime(target.getSnippet().getPublishedAt());

        return Optional.of(new YoutubeVideo(videoTitle, videoId, url, publishDate, description));
    }

    @Override
    boolean onListen() {
        val youtubeVideo = getVideos().get(0);
        this.setLatestVideo(youtubeVideo.getPublishDate());

        return true;
    }
}
