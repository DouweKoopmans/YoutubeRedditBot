package com.fallingdutchman.youtuberedditbot.listeners.filtering;

import com.fallingdutchman.youtuberedditbot.model.YoutubeVideo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by douwe on 7-10-16.
 */
@RunWith(value = Parameterized.class)
public class DescriptionFilterTest {

    private static final String channelName = "test123";
    private static final String channelId = "foobar";
    private final String singleLineDescription;
    private VideoFilter descriptionFilter;
    private YoutubeVideo youtubeVideo;

    public DescriptionFilterTest(String singleLineDescription) {
        this.singleLineDescription = singleLineDescription;
    }

    @Before
    public void setUp() {
        descriptionFilter = new FilterFactoryImpl().createDescriptionFilter(channelId, channelName);
    }

    @After
    public void tearDown() {
        descriptionFilter = null;
        youtubeVideo = null;
    }

    @Test
    public void checkEntrySingleLine() throws Exception {
        final String description = String.format("https://www.youtube.com/%s", singleLineDescription);
        this.youtubeVideo = new YoutubeVideo("test", "test", new URL("http://www.google.com"), LocalDateTime.now(), description);

        Assert.assertTrue("did not detect simple singleLineDescription", descriptionFilter.test(youtubeVideo));
    }

    @Test
    public void checkEntryMultiline() throws Exception {
        final String description = String.format("test123\n\n foo https://www.youtube.com/%s bar\ntest",
                singleLineDescription);
        this.youtubeVideo = new YoutubeVideo("test", "test", new URL("http://www.google.com"), LocalDateTime.now(), description);


        Assert.assertTrue(descriptionFilter.test(youtubeVideo));
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"user/" + channelName},
                {channelName},
                {"channel/" + channelId}
        });
    }
}