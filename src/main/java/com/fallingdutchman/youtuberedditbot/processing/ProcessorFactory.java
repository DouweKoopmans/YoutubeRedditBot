package com.fallingdutchman.youtuberedditbot.processing;

import com.fallingdutchman.youtuberedditbot.model.Instance;
import com.fallingdutchman.youtuberedditbot.model.YoutubeVideo;

/**
 * Created by douwe on 11-1-17.
 */
public interface ProcessorFactory {
    YoutubeProcessor create(Instance instance, YoutubeVideo video);
}
