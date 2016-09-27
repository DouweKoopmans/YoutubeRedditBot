package com.fallingdutchman.youtuberedditbot.authentication.reddit.old;

import org.scribe.model.OAuthRequest;

/**
 * Created by Douwe Koopmans on 13-1-16.
 */
@FunctionalInterface
public interface RedditRequest {
    OAuthRequest makeRequest();
}
