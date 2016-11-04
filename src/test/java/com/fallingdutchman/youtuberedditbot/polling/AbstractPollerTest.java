package com.fallingdutchman.youtuberedditbot.polling;

import com.fallingdutchman.youtuberedditbot.YoutubeVideo;
import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManager;
import com.fallingdutchman.youtuberedditbot.config.ConfigHandler;
import com.fallingdutchman.youtuberedditbot.listeners.YoutubeRssFeedListener;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Douwe Koopmans on 22-1-16.
 */
public class AbstractPollerTest {
    private AbstractPoller poller;

    @Before
    public void setUp() throws Exception {
        RedditManager authenticator = new RedditManager("fake");
        authenticator.shouldAuth = false;
        YoutubeRssFeedListener listener = YoutubeRssFeedListener.of(ConfigHandler.getInstance().createInstance(
                "fake",
                "fake",
                "fake",
                Lists.asList("fake", new String[0]),
                false,
                1, "", ""), authenticator);

        poller = new AbstractPoller(listener) {
            @Override
            protected void runPoller(int entries) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Test
    public void testScanForNewEntries() throws Exception {
        LocalDateTime future = LocalDateTime.now().plusMinutes(2);

        YoutubeVideo entry1 = new YoutubeVideo("", "", null, "", future);
        YoutubeVideo entry2 = new YoutubeVideo("", "", null, "", LocalDateTime.now().minusMinutes(2));

        List<YoutubeVideo> entries = Lists.newArrayList(entry1, entry2);

        assertEquals(1, poller.scanForNewEntries(entries));

        entries.remove(entry1);
        entry1 = new YoutubeVideo("", "", null, "", LocalDateTime.now().minusMinutes(2));
        entries.add(entry1);

        assertEquals(0, poller.scanForNewEntries(entries));
    }
}