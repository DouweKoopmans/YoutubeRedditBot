package com.fallingdutchman.youtuberedditbot;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Created by douwe on 6-10-16.
 */
public class FeedRegister {
    private static final Logger log = LoggerFactory.getLogger(FeedRegister.class);
    private final Set<FeedListener> entries = Sets.newConcurrentHashSet();

    public Set<FeedListener> getEntries() {
        return ImmutableSet.copyOf(entries);
    }

    public void addEntry(FeedListener entry) {
        entries.add(entry);
    }

    public void removeEntry(FeedListener entry) {
        entries.remove(entry);
    }

    public void print() {
        log.info(String.format("there are currently %s feeds registered", entries.size()));
        log.info("printing info");
        log.info("-----------------------------------------------------");
        entries.forEach(feedListener -> {
            log.info(feedListener.toString());
            log.info("-----------------------------------------------------");
        });
    }
}
