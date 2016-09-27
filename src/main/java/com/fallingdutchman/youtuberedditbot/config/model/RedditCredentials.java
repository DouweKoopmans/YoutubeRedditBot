package com.fallingdutchman.youtuberedditbot.config.model;

/**
 * Created by douwe on 20-9-16.
 */
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

    public String getRedditClientId() {
        return redditClientId;
    }

    public String getRedditOauthSecret() {
        return redditOauthSecret;
    }

    public String getRedditUserName() {
        return redditUserName;
    }

    public String getRedditPassword() {
        return redditPassword;
    }
}
