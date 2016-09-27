package com.fallingdutchman.youtuberedditbot.authentication.reddit.old;

import com.google.common.base.Objects;
import com.google.gson.JsonElement;

import java.io.IOException;

/**
 * Created by douwe on 20-9-16.
 */
public class RedditUser {
    private final String authToken;
    private final String authSecret;
    private final String username;
    private String modHash;

    public RedditUser(String authToken, String authSecret, String username) throws IOException {
        this.authToken = authToken;
        this.authSecret = authSecret;
        this.username = username;

        updateModHash();
    }

    public void updateModHash() throws IOException {
        JsonElement modHashJson = RedditSenderManager.getInstance().getJsonFromUrl("https://www.reddit.com/api/me.json");
        modHash = modHashJson.getAsJsonObject().get("date").getAsJsonObject().get("modhash").getAsString();
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getAuthSecret() {
        return authSecret;
    }

    public String getUsername() {
        return username;
    }

    public String getModHash() {
        return modHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RedditAuthenticationRegister.Authentication)) return false;
        RedditAuthenticationRegister.Authentication that = (RedditAuthenticationRegister.Authentication) o;
        return Objects.equal(getAuthToken(), that.getAuthToken()) &&
                Objects.equal(getAuthSecret(), that.getAuthSecret()) &&
                Objects.equal(getUsername(), that.getUsername()) &&
                Objects.equal(getModHash(), that.getModHash());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getAuthToken(), getAuthSecret(), getUsername(), modHash);
    }
}
