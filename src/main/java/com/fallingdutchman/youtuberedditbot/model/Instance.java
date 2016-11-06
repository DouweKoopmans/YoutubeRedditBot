package com.fallingdutchman.youtuberedditbot.model;

import lombok.NonNull;
import lombok.Value;

import java.util.List;

/**
 * Created by douwe on 20-9-16.
 */
@Value
public final class Instance {
    String youtubeName;
    @NonNull String pollerType;
    @NonNull String channelId;
    @NonNull List<String> subreddits;
    @NonNull boolean postComment;
    @NonNull double pollerInterval;
    String apiKey;
    @NonNull String listenerType;
    @NonNull List<CommentRule> commentRules;
    String commentFormatPath;
}
