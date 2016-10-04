package com.fallingdutchman.youtuberedditbot.polling;

import com.fallingdutchman.youtuberedditbot.YoutubeFeedListener;
import com.fallingdutchman.youtuberedditbot.authentication.reddit.jraw.RedditManager;
import com.fallingdutchman.youtuberedditbot.config.ConfigHandler;
import com.fallingdutchman.youtuberedditbot.YrbUtils;
import com.google.common.collect.Lists;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
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
        YoutubeFeedListener listener = YoutubeFeedListener.of(ConfigHandler.getInstance().createInstance(
                "fake",
                "fake",
                "fake",
                Lists.asList("fake", new String[0]),
                false
        ), authenticator);

        poller = new AbstractPoller(listener) {
            @Override
            protected void runPoller(int entries) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Test
    public void testScanForNewEntries() throws Exception {
        SyndEntry entry1 = new SyndEntryImpl();
        SyndEntry entry2 = new SyndEntryImpl();

        LocalDateTime future = LocalDateTime.now().plusDays(2);

        entry1.setPublishedDate(YrbUtils.localDateToDate(future));
        entry2.setPublishedDate(YrbUtils.localDateToDate(LocalDateTime.now()));

        List<SyndEntry> entries = Lists.newArrayList(entry1, entry2);

        assertEquals(1, poller.scanForNewEntries(entries));

        entry1.setPublishedDate(YrbUtils.localDateToDate(LocalDateTime.now()));

        assertEquals(0, poller.scanForNewEntries(entries));
    }
}