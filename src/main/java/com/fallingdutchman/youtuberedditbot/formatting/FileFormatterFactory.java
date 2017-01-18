package com.fallingdutchman.youtuberedditbot.formatting;

import com.fallingdutchman.youtuberedditbot.model.AppConfig;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import lombok.NonNull;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by douwe on 3-10-16.
 */
public class FileFormatterFactory implements FormatterFactory{
    private final AppConfig.Formatting formattingConfig;

    public FileFormatterFactory(@NonNull final AppConfig config) {
        this.formattingConfig = config.getFormatting();
    }


    @Override
    public Formatter createFormatterFromPath(final String path) throws IOException {
        return new Formatter(joinStrings(Files.readAllLines(Paths.get(generateFileLocation(path)))));
    }

    @Override
    public Formatter createFormatterFromFile(@NonNull File file) throws IOException {
        return new Formatter(joinStrings(Files.readAllLines(Paths.get(file.toURI()))));
    }

    String generateFileLocation(@NonNull final String path) {
        Preconditions.checkArgument(!path.isEmpty(), "file path can't be empty");
        Preconditions.checkArgument(!path.endsWith("."));
        Preconditions.checkArgument(!path.startsWith("."));

        return formattingConfig.getFolder() + path.toLowerCase() + '.' + formattingConfig.getFileExtension();
    }

    @Nonnull
    static String joinStrings(@NonNull final List<String> strings){
        return Joiner.on("\n").join(strings);
    }
}
