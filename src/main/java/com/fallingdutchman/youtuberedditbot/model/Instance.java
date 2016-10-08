package com.fallingdutchman.youtuberedditbot.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by douwe on 20-9-16.
 */
public class Instance {
    private final Logger log = LoggerFactory.getLogger(Instance.class);
    private final String youtubeName;
    private final String type;
    private final String channelId;
    private final List<String> subreddits;

    private final boolean postDescription;

    public Instance(String type, String channelId, String youtubeName,
                    List<String> subreddits, boolean postDescription) {

        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(channelId);
        Preconditions.checkNotNull(subreddits);
        Preconditions.checkArgument(!subreddits.isEmpty());
        this.type = type;
        this.channelId = channelId;
        this.youtubeName = youtubeName;
        this.subreddits = subreddits;
        this.postDescription = postDescription;
    }

    public String getChannelId() {
        return channelId;
    }

    public List<String> getSubreddits() {
        return subreddits;
    }

    public String getType() {
        return type;
    }

    public String getYoutubeName() {
        return youtubeName;
    }

    public boolean shouldPostDescription() {
        return postDescription;
    }

    public void print() {
        log.info("Type: " + getType());
        log.info("Subreddits: " + getSubreddits());
        log.info("YoutubeFeed: " + getChannelId());
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
        return Objects.equal(getChannelId(), instance.getChannelId()) &&
                Objects.equal(getSubreddits(), instance.getSubreddits());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getChannelId(), getSubreddits());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .add("subreddits", subreddits)
                .add("channelId", channelId)
                .add("youtubeName", youtubeName)
                .toString();
    }
}
