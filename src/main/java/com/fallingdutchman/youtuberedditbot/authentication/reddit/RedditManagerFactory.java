package com.fallingdutchman.youtuberedditbot.authentication.reddit;

/**
 * Created by douwe on 11-1-17.
 */
public interface RedditManagerFactory {
    RedditManager create(String username);
}
