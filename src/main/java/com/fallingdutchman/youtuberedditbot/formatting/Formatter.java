package com.fallingdutchman.youtuberedditbot.formatting;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.util.Map;

/**
 * Created by douwe on 2-10-16.
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Formatter {
    protected final String template;

    public String format(final Map<String, String> values) {
        val sub = new StrSubstitutor(values);

        return sub.replace(template);
    }
}