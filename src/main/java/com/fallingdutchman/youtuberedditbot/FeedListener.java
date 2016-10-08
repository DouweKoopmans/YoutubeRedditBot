package com.fallingdutchman.youtuberedditbot;

/**
 * Created by Douwe Koopmans on 10-1-16.
 */
public interface FeedListener {

    void stopListening();

    void listen();
}
