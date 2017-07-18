package com.fallingdutchman.youtuberedditbot.processing;

import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManagerRegistry;
import com.fallingdutchman.youtuberedditbot.authentication.youtube.YoutubeManager;
import com.fallingdutchman.youtuberedditbot.history.HistoryManager;
import com.fallingdutchman.youtuberedditbot.model.AppConfig;
import com.fallingdutchman.youtuberedditbot.model.Instance;
import com.fallingdutchman.youtuberedditbot.model.Post;
import com.fallingdutchman.youtuberedditbot.model.YoutubeVideo;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by douwe on 19-6-17.
 */
// TODO: 19-6-17
public class YoutubeLinkReplacerTest {
    @Test
    public void replaceYtLinkInHistory() throws Exception {
        List<Post> mockPosts = new ArrayList<>();
        mockPosts.add(new Post(new YoutubeVideo("title", "GYVcte-6RPg",
                new URL("http://www.youtube.com/watch?v=GYVcte-6RPg"), LocalDateTime.now(),
                "www.youtube.com/watch?v=GYVcte-6RPg"), "reddit.com/foobar"));

        //mocking
        final HistoryManager mockHistoryManager = mock(HistoryManager.class);
        when(mockHistoryManager.getHistory()).thenReturn(mockPosts);

        final Instance mockConfigInstance =  new Instance("fakeChannel", new Instance.Comment("description",
                true, Lists.newArrayList(new Instance.Comment.CommentRule("foo", "bar"))),
                "new-video", new Instance.RedditCredentials("fake", "fake",
                "fake", "foobar"), Lists.newArrayList("foobar"), "fake", null,
                1, "api");

        final AppConfig mockAppconfig = new AppConfig(new AppConfig.History("", ""), new AppConfig.Formatting("md", "data/formats/"),
                new AppConfig.RedditConfig("fake", "fake", false), new AppConfig.UserConfig("data/", "",
                "", "", ""), new AppConfig.YoutubeConfig(false, "",
                0L), new AppConfig.ListenerConfig(0));

        final RedditManagerRegistry mockRedditManagerRegistry = mock(RedditManagerRegistry.class);
        when(mockRedditManagerRegistry.getManager("foobar")).thenReturn(null);

        final YoutubeVideo mockVideo = new YoutubeVideo("", "", new URL("http://google.com"),
                LocalDateTime.now(), "");

        final YoutubeManager mockYtManager = mock(YoutubeManager.class);

        final YoutubeProcessor processor = new YoutubeProcessor(mockVideo, mockConfigInstance,
                mockAppconfig, mockRedditManagerRegistry, mockHistoryManager, mockYtManager);

        final String testDescription = "www.youtube.com/watch?v=GYVcte-6RPg";
        final String expected = "[title](http://www.youtube.com/watch?v=GYVcte-6RPg) \n" +
                "[ ^^\\(reddit ^^discussion)](reddit.com/foobar)  ";

        assertEquals(expected, processor.replaceYtLinks(testDescription));
    }

    @Test
    public void testYtLinkNotInHistory() throws Exception {
        final String videoId = "GYVcte-6RPg";
        final String videoTitle = "title";

        //history manager
        final HistoryManager mockHistoryManager = mock(HistoryManager.class);
        when(mockHistoryManager.getHistory()).thenReturn(new ArrayList<>());

        //config instance
        final Instance fakeConfigInstance =  new Instance("fakeChannel", new Instance.Comment("description",
                true, Lists.newArrayList(new Instance.Comment.CommentRule("foo", "bar"))),
                "new-video", new Instance.RedditCredentials("fake", "fake",
                "fake", "foobar"), Lists.newArrayList("foobar"), "fake", null,
                1, "api");

        //App Config
        final AppConfig fakeAppConfig = new AppConfig(new AppConfig.History("", ""), new AppConfig.Formatting("md", "data/formats/"),
                new AppConfig.RedditConfig("fake", "fake", false), new AppConfig.UserConfig("data/", "",
                "", "", ""), new AppConfig.YoutubeConfig(false, "",
                0L), new AppConfig.ListenerConfig(0));

        //Reddit Manager Registry
        final RedditManagerRegistry mockRedditManagerRegistry = mock(RedditManagerRegistry.class);
        when(mockRedditManagerRegistry.getManager("foobar")).thenReturn(null);

        //Youtube Video
        final YoutubeVideo fakeVideo = new YoutubeVideo("", "", new URL("http://google.com"), LocalDateTime.now(), "");

        //Youtube Manager
        final VideoSnippet videoSnippet = new VideoSnippet().setTitle(videoTitle);
        final Video testVideo = new Video().setSnippet(videoSnippet);
        final YoutubeManager mockYtManager = mock(YoutubeManager.class);
        when(mockYtManager.getVideoDataFromVideoId(eq(videoId), anyString())).thenReturn(Optional.of(testVideo));

        //Youtube Processor
        final YoutubeProcessor processor = new YoutubeProcessor(fakeVideo, fakeConfigInstance, fakeAppConfig,
                mockRedditManagerRegistry, mockHistoryManager, mockYtManager);

        final String testDescription = "http://www.youtube.com/watch?v=" + videoId;
        final String expected = "[" + videoTitle + "](http://www.youtube.com/watch?v=" + videoId + ") \n";

        assertEquals(expected, processor.replaceYtLinks(testDescription));
    }

    @Test
    public void testYoutubePattern() throws Exception {
        final Pattern testPattern = Pattern.compile(YoutubeProcessor.youtubeLinkPattern);
        final Set<String> positiveTestStrings = new HashSet<>();
        final Set<String> negativeTestStrings = new HashSet<>();

        positiveTestStrings.add("www.youtube.com/watch?v=GYVcte-6RPg");
        positiveTestStrings.add("http://www.youtube.com/watch?v=GYVcte-6RPg");
        positiveTestStrings.add("https://www.youtube.com/watch?v=GYVcte-6RPg");
        positiveTestStrings.add("https://youtube.com/watch?v=GYVcte-6RPg");
        positiveTestStrings.add("https://youtu.be/GYVcte-6RPg");
        positiveTestStrings.add("http://youtu.be/GYVcte-6RPg");
        positiveTestStrings.add("www.youtu.be/GYVcte-6RPg");

        negativeTestStrings.add("www.youtube.com/user/FooBar");
        negativeTestStrings.add("www.youtube.com/channel/foobarfoobarfoobar");
        negativeTestStrings.add("http://www.youtube.com/subscription_center?add_user=foobar");
        negativeTestStrings.add("http://www.youtube.com/c/foobar");
        negativeTestStrings.add("http://www.foobar.com/watch?v=GYVcte-6RPg");


        positiveTestStrings.forEach(s -> {
            final Matcher matcher = testPattern.matcher(s);
            if (!matcher.find()) {
                fail("failed to match string: " + s);
            }

            String videoId = matcher.group(1);

            if (!"GYVcte-6RPg".equals(videoId)) {
                fail("did not find right video id for string: " + s
                        + " expected GYVcte-6RPg but got " + videoId);
            }
        });

        negativeTestStrings.forEach(s -> {
            if (testPattern.matcher(s).find()) {
                fail("false positive on string: " + s);
            }
        });
    }
}