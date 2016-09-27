package com.fallingdutchman.youtuberedditbot.config.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by douwe on 20-9-16.
 */
public class Instance {
    private final Logger log = LoggerFactory.getLogger(Instance.class);
    @Nullable
    private final String youtubeName;
    @Nonnull
    private final String type;
    @Nonnull
    private final String youtubeFeed;
    @Nonnull
    private final List<String> subreddits;
    @Nullable
    private final String descriptionRegex;

    public Instance(@Nonnull String type, @Nonnull String youtubeFeed,
                    @Nullable String descriptionRegex, @Nullable String youtubeName,
                    @Nonnull List<String> subreddits) {

        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(youtubeFeed);
        Preconditions.checkNotNull(subreddits);
        Preconditions.checkArgument(!subreddits.isEmpty());
        this.type = type;
        this.youtubeFeed = youtubeFeed;
        this.descriptionRegex = descriptionRegex;
        this.youtubeName = youtubeName;
        this.subreddits = subreddits;
    }

    @Nonnull
    public String getYoutubeFeed() {
        return youtubeFeed;
    }

    @Nullable
    public String getDescriptionRegex() {
        return descriptionRegex;
    }

    @Nonnull
    public List<String> getSubreddits() {
        return subreddits;
    }

    @Nonnull
    public String getType() {
        return type;
    }

    @Nullable
    public String getYoutubeName() {
        return youtubeName;
    }

    public void print() {
        log.info("Type: " + getType());
        log.info("Subreddits: " + getSubreddits());
        log.info("YoutubeFeed: " + getYoutubeFeed());
        log.info("DescriptionRegex: " + getDescriptionRegex());
        log.info("YoutubeName: " + getYoutubeName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (!(o instanceof Instance)){
            return false;
        }
        Instance instance = (Instance) o;
        return Objects.equal(getYoutubeFeed(), instance.getYoutubeFeed()) &&
                Objects.equal(getDescriptionRegex(), instance.getDescriptionRegex()) &&
                Objects.equal(getSubreddits(), instance.getSubreddits());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getYoutubeFeed(), getDescriptionRegex(), getSubreddits());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .add("subreddits", subreddits)
                .add("youtubeFeed", youtubeFeed)
                .add("descriptionRegex", descriptionRegex)
                .add("youtubeName", youtubeName)
                .toString();
    }
}
