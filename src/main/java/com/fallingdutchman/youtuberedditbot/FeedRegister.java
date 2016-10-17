package com.fallingdutchman.youtuberedditbot;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * Created by douwe on 6-10-16.
 */
@Slf4j
public class FeedRegister {
    private final Set<FeedListener> entries = Sets.newConcurrentHashSet();

    public Set<FeedListener> getEntries() {
        return ImmutableSet.copyOf(entries);
    }

    public void addEntry(FeedListener entry) {
        log.info("registered listener: " + entry);
        entries.add(entry);
    }

    public void removeEntry(FeedListener entry) {
        log.debug("removing registry entry for listener " + entry);
        if (entries.remove(entry)) {
            log.info("removed registry entry " + entry);
        }
    }

    public void print() {
        log.info("there are currently {} feeds registered", entries.size());
        log.info("printing info");
        log.info("-----------------------------------------------------");
        entries.forEach(feedListener -> {
            log.info(feedListener.toString());
            log.info("-----------------------------------------------------");
        });
    }
}
