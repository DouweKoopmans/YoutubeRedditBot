package com.fallingdutchman.youtuberedditbot.formatting;

import java.io.File;
import java.io.IOException;

/**
 * Created by douwe on 3-10-16.
 */
public interface FormatterFactory {
    Formatter createFormatterFromPath(final String path) throws IOException;

    Formatter createFormatterFromFile(final File file) throws IOException;
}
