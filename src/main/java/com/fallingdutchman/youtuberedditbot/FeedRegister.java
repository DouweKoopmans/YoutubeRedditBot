package com.fallingdutchman.youtuberedditbot;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import org.javatuples.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * basic implementation of the FeedRegister interface using Integers as the identifier and a concurrent map for
 * storing the key-value pairs
 *
 * Created by Douwe Koopmans on 10-1-16.
 */
public class FeedRegister implements Iterable<KeyValue<Integer, IFeedListener>> {
    private static final Logger log = LoggerFactory.getLogger(FeedRegister.class);
    private BiMap<Integer, IFeedListener> feeds = Maps.synchronizedBiMap(HashBiMap.create());
    private boolean idGap;
    private static FeedRegister instance = new FeedRegister();

    private FeedRegister(){
    }

    public static FeedRegister getInstance() {
        return instance;
    }

    public void add(IFeedListener feed) {
        int id = getNextAvailableId();

        log.info(String.format("registering new feed for channel id %1$s to %2$s", feed.getChannelId(), id));
        feeds.put(id, feed);
    }

    public void remove(Integer id) {
        if (!isUsed(id)) {
            throw new IllegalArgumentException("that key is not in use");
        }

        synchronized (feeds.get(id)) {
            feeds.get(id).stopListening();
            feeds.remove(id);
            idGap = true;
        }
    }

    public IFeedListener get(Integer id) {
        if (!isUsed(id)) {
            throw new IllegalArgumentException("that key is not in use");
        }

        return feeds.get(id);
    }

    public Integer keyOf(IFeedListener feedListener) {
        return feeds.inverse().get(feedListener);
    }

    public void print() {
        log.info(String.format("there are currently %s feeds registered", feeds.size()));

        feeds.forEach((integer, feedListener) -> {
            log.info("ID: " + integer);
            feedListener.print();
            log.info("----------------------------------------------------");
        });
    }

    private int getNextAvailableId(){
        synchronized (getInstance()) {
            if (idGap) {
                for (int i = 1; ; i++) {
                    if (!isUsed(i)) {
                        return i;
                    }
                }
            } else {
                return feeds.size() + 1;
            }
        }
    }

    private boolean isUsed(int id) {
        return id > 0 && feeds.get(id) != null;
    }

    @Override
    public Iterator<KeyValue<Integer, IFeedListener>> iterator() {
        return feeds.entrySet()
                .stream()
                .map(o -> new KeyValue<>(o.getKey(), o.getValue()))
                .iterator();
    }
}
