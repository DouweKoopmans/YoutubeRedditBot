package com.fallingdutchman.youtuberedditbot.formatting;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.util.Map;

/**
 * Created by douwe on 2-10-16.
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Formatter {
    private final String template;

    public String format(final Map<String, String> values) {
        return new StrSubstitutor(values).replace(template);
    }
}