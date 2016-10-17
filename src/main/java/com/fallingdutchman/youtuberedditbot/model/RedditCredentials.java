package com.fallingdutchman.youtuberedditbot.model;

import lombok.Data;

/**
 * Created by douwe on 20-9-16.
 */
@Data
public class RedditCredentials {
    private final String redditClientId;
    private final String redditOauthSecret;
    private final String redditUserName;
    private final String redditPassword;

    public RedditCredentials(String redditClientId, String redditOauthSecret, String redditUserName,
                             String redditPassword) {
        this.redditClientId = redditClientId;
        this.redditOauthSecret = redditOauthSecret;
        this.redditUserName = redditUserName;
        this.redditPassword = redditPassword;
    }
}
