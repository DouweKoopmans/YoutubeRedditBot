package com.fallingdutchman.youtuberedditbot.history;

import com.fallingdutchman.youtuberedditbot.model.AppConfig;
import com.fallingdutchman.youtuberedditbot.model.Post;
import com.fallingdutchman.youtuberedditbot.model.Video;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.dean.jraw.models.Submission;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by douwe on 16-6-17.
 */
@Slf4j
@Singleton
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HistoryManager {
    List<Post> history;
    File file;

    @Inject
    public HistoryManager(@NonNull final AppConfig config) throws IOException {
        val historyConfig = config.getHistory();

        file = new File(historyConfig.getFolder() + LocalDate.now().toString() + '.' + historyConfig.getFileExtension());
        if (!file.exists()) {
            createFile(file);
        }
        this.history = loadHistory(listFilesFromFolder(historyConfig.getFolder()));
    }

    public void addPost(@NonNull final Video video, @NonNull final Submission submission) throws IOException {
        addPost(new Post(video, submission.getPermalink()));
    }

    public void addPost(@NonNull final Post post) throws IOException {
        history.add(post);
        updateFile();
    }

    @Synchronized("history")
    protected void updateFile() throws FileNotFoundException {
        val json = new GsonBuilder().create().toJson(history);
        @Cleanup val pw = new PrintWriter(this.file);
        pw.write(json);
    }

    public List<Post> getHistory() {
        return ImmutableList.copyOf(history);
    }

    private List<File> listFilesFromFolder(final String folder) {
        List<File> result = Lists.newArrayList();
        val files = new File(folder).listFiles();

        if (files != null) {
            result.addAll(Arrays.asList(files));
        }

        return result;
    }

    private void createFile(@NonNull File target) throws IOException {
        target.getParentFile().mkdirs();
        target.createNewFile();

        @Cleanup val pw = new PrintWriter(target);
        pw.write(new GsonBuilder().create().toJson(new ArrayList<Post>()));
    }

    @SneakyThrows(FileNotFoundException.class)
    private List<Post> loadHistory(final List<File> files) throws IOException {
        final List<Post> posts = Lists.newArrayList();

        for (File f : files) {
            if (f.isDirectory()) {
                continue;
            }
            try {
                @Cleanup final FileReader in = new FileReader(f);
                @Cleanup final JsonReader reader = new JsonReader(in);
                posts.addAll(new GsonBuilder().create().fromJson(reader, new TypeToken<ArrayList<Post>>() {}.getType()));
            } catch (IllegalStateException ex) {
                log.error("incorrect Json file found in history folder, name: " + f.getName());
            }
        }

        return posts;
    }
}
