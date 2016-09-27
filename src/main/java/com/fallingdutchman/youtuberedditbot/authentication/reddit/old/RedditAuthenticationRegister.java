package com.fallingdutchman.youtuberedditbot.authentication.reddit.old;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.util.Set;

/**
 * Created by Douwe Koopmans on 26-1-16.
 */
public class RedditAuthenticationRegister {
    Set<Authentication> register = Sets.newConcurrentHashSet();

    public void add(String authToken, String authSecret, String username) throws IOException{
        register.add(new Authentication(authToken, authSecret, username));
    }

    public void remove(String username) throws IllegalArgumentException {
        register.stream().filter(authentication -> authentication.getUsername().equals(username))
                .forEach(authentication -> register.remove(authentication));
    }

    public Authentication get(String username) throws IllegalArgumentException {
        for (Authentication authentication : register) {
            if (authentication.getUsername().equals(username)) {
                return authentication;
            }
        }

        throw new IllegalArgumentException(String.format("there is no known authentication for %s", username));
    }

    public class Authentication{
        private final String authToken;
        private final String authSecret;
        private final String username;
        private String modHash;

        public Authentication(String authToken, String authSecret, String username) throws IOException {
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
            if (!(o instanceof Authentication)) return false;
            Authentication that = (Authentication) o;
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
}
