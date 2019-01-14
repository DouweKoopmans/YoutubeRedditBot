package com.fallingdutchman.youtuberedditbot.formatting;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.commons.text.StringSubstitutor;

import java.util.Map;

/**
 * Created by douwe on 2-10-16.
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Formatter {
    private final String template;

    public String format(final Map<String, String> values) {
        return new StringSubstitutor(values).replace(template);
    }
}