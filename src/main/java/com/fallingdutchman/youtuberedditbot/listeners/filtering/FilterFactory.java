package com.fallingdutchman.youtuberedditbot.listeners.filtering;

import com.fallingdutchman.youtuberedditbot.model.Instance;
import lombok.NonNull;

/**
 * Created by douwe on 18-1-17.
 */
public interface FilterFactory {
    VideoFilter create(@NonNull Instance instance);
}
