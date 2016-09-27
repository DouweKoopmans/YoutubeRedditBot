package com.fallingdutchman.youtuberedditbot.listeners;

/**
 * Created by Douwe Koopmans on 10-1-16.
 */
public interface IFeedListener {

    void stopListening();

    void print();

    String getChannelId();

    void listen();
}
