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
    protected final String template;

    public String format(final Map<String, String> values) {
        StrSubstitutor sub = new StrSubstitutor(values);
        String comment = sub.replace(template);
        comment = comment.replaceAll("\n", "  \n");

        return comment;
    }
}