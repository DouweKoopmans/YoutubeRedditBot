package com.fallingdutchman.youtuberedditbot.model;

import com.google.common.base.Preconditions;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Created by douwe on 20-9-16.
 */
@Slf4j
@Data
public final class Instance {
    private final String youtubeName;
    private final String pollerType;
    private final String channelId;
    private final List<String> subreddits;
    private final boolean postDescription;
    private final double pollerInterval;
    private final String apiKey;
    private final String listenerType;

    public Instance(String pollerType, String channelId, String youtubeName, List<String> subreddits,
                    boolean postDescription, double pollerInterval, String apiKey, String listenerType) {
        Preconditions.checkNotNull(pollerType);
        Preconditions.checkNotNull(channelId);
        Preconditions.checkNotNull(subreddits);
        Preconditions.checkArgument(!subreddits.isEmpty());
        Preconditions.checkArgument(pollerInterval >= 0.5);

        this.pollerInterval = pollerInterval;
        this.pollerType = pollerType;
        this.channelId = channelId;
        this.youtubeName = youtubeName;
        this.subreddits = subreddits;
        this.postDescription = postDescription;
        this.apiKey = apiKey;
        this.listenerType = listenerType;
    }
}
