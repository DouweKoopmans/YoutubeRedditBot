package com.fallingdutchman.youtuberedditbot.authentication.reddit;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.Synchronized;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by douwe on 18-1-17.
 */
@Singleton
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedditManagerRegistry {
    Map<String, RedditManager> register;
    RedditManagerFactory factory;

    @Inject
    public RedditManagerRegistry(RedditManagerFactory factory) {
        register = new HashMap<>();
        this.factory = factory;
    }

    @Synchronized
    public RedditManager getManager(String username) {
        if (register.containsKey(username)) {
            return register.get(username);
        } else {
            final RedditManager manager = factory.create(username);
            register.put(username, manager);
            return manager;
        }
    }
}
