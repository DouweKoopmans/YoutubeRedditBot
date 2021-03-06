package com.fallingdutchman.youtuberedditbot.listeners.filtering;

import com.fallingdutchman.youtuberedditbot.model.Video;

import java.util.function.Predicate;

/**
 * Created by douwe on 18-1-17.
 */
@FunctionalInterface
public interface VideoFilter extends Predicate<Video> {
}
