package com.fallingdutchman.youtuberedditbot.model;

import lombok.NonNull;
import lombok.Value;

/**
 * Created by douwe on 16-6-17.
 */
@Value
public class Post implements Comparable<Post> {
    @NonNull YoutubeVideo video;
    @NonNull String permaLink;

    @Override
    public int compareTo(@NonNull Post o) {
        return this.video.compareTo(o.getVideo());
    }
}
