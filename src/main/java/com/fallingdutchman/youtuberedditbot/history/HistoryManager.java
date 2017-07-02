package com.fallingdutchman.youtuberedditbot.history;

import com.fallingdutchman.youtuberedditbot.model.AppConfig;
import com.fallingdutchman.youtuberedditbot.model.Post;
import com.fallingdutchman.youtuberedditbot.model.YoutubeVideo;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.beans.DefaultProperty;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.dean.jraw.models.Submission;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by douwe on 16-6-17.
 */
@Singleton
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HistoryManager {
    List<Post> history;
    File file;

    @Inject
    public HistoryManager(@NonNull final AppConfig config) throws IOException {
        AppConfig.History historyConfig = config.getHistory();

        file = new File(historyConfig.getFolder() + LocalDate.now().toString() + '.' + historyConfig.getFileExtension());
        if (!file.exists()) {
            createFile(file);
        }
        this.history = loadHistory();
    }

    public void addPost(@NonNull final YoutubeVideo video, @NonNull final Submission submission) throws IOException {
        addPost(new Post(video, submission.getPermalink()));
    }

    public void addPost(@NonNull final Post post) throws IOException {
        history.add(post);
        updateFile();
    }

    protected void updateFile() throws FileNotFoundException {
        synchronized (history) {
            val json = new GsonBuilder().create().toJson(history);
            val pw = new PrintWriter(this.file);
            pw.write(json);
            pw.close();
        }
    }

    public List<Post> getHistory() {
        return ImmutableList.copyOf(history);
    }

    private void createFile(@NonNull File target) throws IOException {
        target.getParentFile().mkdirs();
        target.createNewFile();

        val pw = new PrintWriter(target);
        val gson = new GsonBuilder().create();
        pw.write(gson.toJson(new ArrayList<Post>()));
        pw.close();
    }

    private List<Post> loadHistory() throws IOException {
        final JsonReader reader = new JsonReader(new FileReader(this.file));
        final List<Post> posts = new GsonBuilder().create().fromJson(reader, new TypeToken<ArrayList<Post>>() {}.getType());
        reader.close();

        return posts;
    }
}
