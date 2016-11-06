package com.fallingdutchman.youtuberedditbot.model;

import lombok.Value;
import lombok.NonNull;

/**
 * Created by douwe on 6-11-16.
 */
@Value
public class CommentRule {
    @NonNull String find;
    @NonNull String replace;
}
