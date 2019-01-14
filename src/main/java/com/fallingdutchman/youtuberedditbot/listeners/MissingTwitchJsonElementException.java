package com.fallingdutchman.youtuberedditbot.listeners;

/**
 * Thrown when a JSON element is missing from an JSON response from the Twitch API
 */
public class MissingTwitchJsonElementException extends RuntimeException {
    private static final String MESSAGE = "element %s missing from JSON";

    MissingTwitchJsonElementException(String element) {
        super(formatMessage(element));
    }

    MissingTwitchJsonElementException(String element, Throwable cause) {
        super(formatMessage(element), cause);
    }

    MissingTwitchJsonElementException(Throwable cause) {
        super(cause);
    }

    private static String formatMessage(String el) {
        return String.format(MESSAGE, el);
    }
}
