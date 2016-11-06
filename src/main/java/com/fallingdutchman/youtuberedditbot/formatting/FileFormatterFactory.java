package com.fallingdutchman.youtuberedditbot.formatting;

import com.fallingdutchman.youtuberedditbot.YrbUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import lombok.val;

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
    public Formatter createFormatterFromPath(final String path) throws IOException {
        val strings = joinStrings(Files.readAllLines(Paths.get(generateFileLocation(path))));

        return new Formatter(strings);
    }

    @Override
    public Formatter createFormatterFromFile(File file) throws IOException {
        return new Formatter(joinStrings(Files.readAllLines(Paths.get(file.toURI()))));
    }

    static String generateFileLocation(final String path) throws FileNotFoundException {
        Preconditions.checkNotNull(path, "file path can't be null");
        Preconditions.checkArgument(!path.isEmpty(), "file path can't be empty");
        Preconditions.checkArgument(!path.endsWith("."));
        Preconditions.checkArgument(!path.startsWith("."));

        return YrbUtils.LOCAL_HOST_FOLDER + FORMAT_FOLDER + "/" + path.replaceAll("/", "").toLowerCase(Locale.UK) +
                '.' + FORMAT_EXTENTION;
    }

    static String joinStrings(final List<String> strings) throws IOException{
        Preconditions.checkNotNull(strings, "input string list can't be null");
        return Joiner.on("\n").join(strings);
    }
}
