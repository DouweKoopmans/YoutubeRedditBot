package com.fallingdutchman.youtuberedditbot.authentication.reddit;

import com.fallingdutchman.youtuberedditbot.model.Instance;

/**
 * Created by douwe on 11-1-17.
 */
public interface RedditManagerFactory {
    RedditManager create(String username, Instance.RedditCredentials credentials);
}
