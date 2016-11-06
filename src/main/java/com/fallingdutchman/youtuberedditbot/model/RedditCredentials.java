package com.fallingdutchman.youtuberedditbot.model;

import lombok.NonNull;
import lombok.Value;

/**
 * Created by douwe on 20-9-16.
 */
@Value
public class RedditCredentials {
    @NonNull String redditClientId;
    @NonNull String redditOauthSecret;
    @NonNull String redditUserName;
    @NonNull String redditPassword;
}
