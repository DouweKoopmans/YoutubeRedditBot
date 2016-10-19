package com.fallingdutchman.youtuberedditbot.formatting;

import com.fallingdutchman.youtuberedditbot.YrbUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

/**
 * Created by douwe on 3-10-16.
 */
public class FileFormatterFactory implements FormatterFactory{
    private static final String FORMAT_FOLDER = "formats";
    private static final String FORMAT_EXTENTION = "md";

    @Override
    public Formatter createFormatterFromName(final String name) throws IOException {
        final String strings = joinStrings(Files.readAllLines(Paths.get(generateFileLocation(name))));

        return new Formatter(strings);
    }

    @Override
    public Formatter createFormatterFromFile(File file) throws IOException {
        return new Formatter(joinStrings(Files.readAllLines(Paths.get(file.toURI()))));
    }

    static String generateFileLocation(final String name) throws FileNotFoundException {
        Preconditions.checkNotNull(name, "file name can't be null");
        Preconditions.checkArgument(!name.isEmpty(), "file name can't be empty");
        Preconditions.checkArgument(!name.endsWith("."));
        Preconditions.checkArgument(!name.startsWith("."));

        return YrbUtils.LOCAL_HOST_FOLDER + FORMAT_FOLDER + "/" + name.replaceAll("/", "").toLowerCase(Locale.UK) +
                '.' + FORMAT_EXTENTION;
    }

    static String joinStrings(final List<String> strings) throws IOException{
        Preconditions.checkNotNull(strings, "input string list can't be null");
        return Joiner.on("\n").join(strings);
    }
}
