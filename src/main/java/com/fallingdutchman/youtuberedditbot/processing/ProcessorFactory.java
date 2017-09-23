package com.fallingdutchman.youtuberedditbot.processing;

import com.fallingdutchman.youtuberedditbot.model.Instance;
import com.fallingdutchman.youtuberedditbot.model.Video;

/**
 * Created by douwe on 11-1-17.
 */
public interface ProcessorFactory {
    VideoProcessor create(Instance instance, Video video);
}
