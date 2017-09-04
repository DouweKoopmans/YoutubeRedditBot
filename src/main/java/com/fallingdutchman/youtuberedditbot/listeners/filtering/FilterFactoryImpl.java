package com.fallingdutchman.youtuberedditbot.listeners.filtering;

import com.fallingdutchman.youtuberedditbot.model.Instance;
import lombok.NonNull;
import lombok.val;

import java.util.regex.Pattern;

/**
 * Created by douwe on 18-1-17.
 */
public class FilterFactoryImpl implements FilterFactory {

    @Override
    public VideoFilter create(@NonNull Instance instance) {
        switch (instance.getPollerType()) {
            case "description-mention":
                return createDescriptionFilter(instance.getTarget().getChannelId(), instance.getTarget().getYoutubeName());
            case "new-video":
            default:
                return createNewVideoFilter();
        }
    }

    VideoFilter createNewVideoFilter() {
        return input -> true;
    }

    VideoFilter createDescriptionFilter(@NonNull String channelId, @NonNull String channelName) {
        return input -> {
            final String regexString = String.format("https?://www\\.youtube\\.com/((user/)?%s|channel/%s)", channelName,
                    channelId);
            val regex = Pattern.compile(regexString, Pattern.UNIX_LINES | Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

            return regex.matcher(input.getDescription()).find();
        };
    }
}
