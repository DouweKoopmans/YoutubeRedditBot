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
    private final String type;
    private final String channelId;
    private final List<String> subreddits;
    private final boolean postDescription;

    public Instance(String type, String channelId, String youtubeName, List<String> subreddits,
                    boolean postDescription) {
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(channelId);
        Preconditions.checkNotNull(subreddits);
        Preconditions.checkArgument(!subreddits.isEmpty());
        this.type = type;
        this.channelId = channelId;
        this.youtubeName = youtubeName;
        this.subreddits = subreddits;
        this.postDescription = postDescription;
    }

    public boolean shouldPostDescription() {
        return postDescription;
    }
}
