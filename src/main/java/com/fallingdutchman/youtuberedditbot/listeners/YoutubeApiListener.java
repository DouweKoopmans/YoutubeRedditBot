package com.fallingdutchman.youtuberedditbot.listeners;

import com.fallingdutchman.youtuberedditbot.YrbUtils;
import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManagerRegistry;
import com.fallingdutchman.youtuberedditbot.authentication.youtube.YoutubeManager;
import com.fallingdutchman.youtuberedditbot.history.HistoryManager;
import com.fallingdutchman.youtuberedditbot.listeners.filtering.FilterFactory;
import com.fallingdutchman.youtuberedditbot.model.AppConfig;
import com.fallingdutchman.youtuberedditbot.model.Instance;
import com.fallingdutchman.youtuberedditbot.model.YoutubeVideo;
import com.fallingdutchman.youtuberedditbot.processing.ProcessorFactory;
import com.google.api.client.util.Lists;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
    AppConfig.YoutubeConfig youtubeConfig;
    YoutubeManager youtubeManager;

    @Inject
    public YoutubeApiListener(@Assisted @NonNull Instance instance, ProcessorFactory processorFactory,
                              @NonNull AppConfig config, RedditManagerRegistry redditRegistry, FilterFactory filterFactory,
                              HistoryManager historyManager, YoutubeManager youtubeManager) {
        super(instance, processorFactory, config, redditRegistry, filterFactory, historyManager);

        this.youtubeManager = youtubeManager;
        this.youtubeConfig = config.getYoutubeConfig();
    }

    @Synchronized
    @Override
    public boolean update() {
        if (!config.getYoutubeConfig().isUpdate())
            return true;

        try {
            List<Channel> channelsList = youtubeManager.listChannelsFromChannelId(getInstance().getChannelId(),
                    getInstance().getYoutubeApiKey());

            if (channelsList != null && !channelsList.isEmpty()) {
                val uploadPlaylistId = channelsList.get(0).getContentDetails().getRelatedPlaylists().getUploads();

                List<PlaylistItem> result = Lists.newArrayList();

                result.addAll(youtubeManager.listAllVideosFromPlayList(uploadPlaylistId, getInstance().getYoutubeApiKey()));

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
