package com.fallingdutchman.youtuberedditbot.listeners;

import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManager;
import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManagerRegistry;
import com.fallingdutchman.youtuberedditbot.listeners.filtering.FilterFactory;
import com.fallingdutchman.youtuberedditbot.model.AppConfig;
import com.fallingdutchman.youtuberedditbot.model.Instance;
import com.fallingdutchman.youtuberedditbot.model.Video;
import com.fallingdutchman.youtuberedditbot.processing.ProcessorFactory;
import com.google.common.collect.Lists;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.runners.Parameterized.Parameter;
import static org.junit.runners.Parameterized.Parameters;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


/**
 * Created by Douwe Koopmans on 22-1-16.
 */
@RunWith(Enclosed.class)
public class ListenerTest {

    public static class UnitTests {
        AbstractVideoListener listener;

        @Before
        public void setUp() throws Exception {
            ProcessorFactory mockProcessorFactory = mock(ProcessorFactory.class);
            RedditManagerRegistry mockRedditRegistry = mock(RedditManagerRegistry.class);
            RedditManager mockRedditManager = mock(RedditManager.class);
            FilterFactory filterFactory = mock(FilterFactory.class);

            when(mockRedditRegistry.getManager(anyString())).thenReturn(mockRedditManager);

            Instance instance = new Instance(true, "", "UCKab3hYnOoTZZbEUQBMx-ww", new Instance.Comment("", false,
                    Lists.newArrayList()), "", new Instance.RedditCredentials("", "", "", "fake"), Lists.newArrayList(),
                    "", new Instance.Target("", ""), 1D, "", "youtube", null, null, null);

            AppConfig appConfig = new AppConfig(new AppConfig.History("", ""), new AppConfig.Formatting("", ""),
                    new AppConfig.RedditConfig("fake", "fake", false), new AppConfig.UserConfig("", "", "", "", ""),
                    new AppConfig.YoutubeConfig(false, "", 0L), new AppConfig.ListenerConfig(0));

            this.listener = new YoutubeRssFeedListener(instance, mockProcessorFactory, appConfig, mockRedditRegistry,
                    filterFactory);
        }

        @Test
        @Ignore
        public void testScanForNewEntries() throws Exception {

            final LocalDateTime future = LocalDateTime.now().plusMinutes(2);
            final LocalDateTime past = LocalDateTime.now().minusMinutes(2);

            Video entry1 = new Video("", "", new URL("http://www.google.com"), future, "");
            Video entry2 = new Video("", "", new URL("http://www.google.com"), past, "");

            List<Video> entries = new ArrayList<>();
            entries.add(entry1);
            entries.add(entry2);

            when(listener.update()).thenReturn(Optional.<List<?>>of(entries));
            when(listener.extract(entry1)).thenReturn(Optional.of(entry1));
            when(listener.extract(entry2)).thenReturn(Optional.of(entry2));

            doNothing().when(listener).handleNewVideo(Mockito.any());
            listener.run();
            verify(listener, times(1)).handleNewVideo(Mockito.any());

            entries.remove(entry1);
            entry1 = new Video("", "", new URL("http://www.google.com"), past, "");
            entries.add(entry1);

            listener.run();
            verify(listener, times(0)).handleNewVideo(Mockito.any());
        }
    }

    @RunWith(org.junit.runners.Parameterized.class)
    public static class Parameterize {

        YoutubeRssFeedListener listener;

        @Parameter
        public SyndEntry entry;

        @Parameters
        public static Collection<Object> data() throws Exception {
            ClassLoader classLoader = ListenerTest.class.getClassLoader();

            final URL resource = classLoader.getResource("test-RssEntry.xml");

            if (resource == null) {
                fail("\"test-RssEntry.xml\" resource not found");
            }

            XmlReader reader = new XmlReader(resource);

            return new ArrayList<>(new SyndFeedInput().build(reader).getEntries());
        }

        @Before
        public void setUp() throws Exception {
            ProcessorFactory mockProcessorFactory = mock(ProcessorFactory.class);
            RedditManagerRegistry mockRedditRegistry = mock(RedditManagerRegistry.class);
            RedditManager mockRedditManager = mock(RedditManager.class);
            FilterFactory filterFactory = mock(FilterFactory.class);

            when(mockRedditRegistry.getManager(anyString())).thenReturn(mockRedditManager);

            Instance instance = new Instance(true, "", "UCKab3hYnOoTZZbEUQBMx-ww", new Instance.Comment("", false,
                    Lists.newArrayList()), "", new Instance.RedditCredentials("", "", "", "fake"), Lists.newArrayList(),
                    "", new Instance.Target("", ""), 1D, "", "", "", "", "");

            AppConfig appConfig = new AppConfig(new AppConfig.History("", ""), new AppConfig.Formatting("", ""),
                    new AppConfig.RedditConfig("fake", "fake", false), new AppConfig.UserConfig("", "", "", "", ""),
                    new AppConfig.YoutubeConfig(false, "", 0L), new AppConfig.ListenerConfig(0));

            this.listener = new YoutubeRssFeedListener(instance, mockProcessorFactory, appConfig, mockRedditRegistry,
                    filterFactory);
        }

        @Test
        public void testRssExtract() throws Exception {
            Optional<Video> expected = Optional.of(new Video("Nerd³ Completes... Watch Dogs 2 - 18 - Marcus Kart", "4iwPS-L3dJc",
                    new URL("http://www.youtube.com/watch?v=4iwPS-L3dJc"),
                    LocalDateTime.of(2017, 1, 11, 20, 0, 0), "\n                HACK_THE_PLANET GaMe_LINk: https://www.ubisoft.com/en-US/game/watch-dogs-2 NERd³_SiTE http://nerdcubed.co.uk NeRd³_PAtrEon: https://www.patreon.com/nerdcubed DAd³_ChaNNeL: http://www.youtube.com/user/OfficialDadCubed TOy_ChanNEL: http://www.youtube.com/user/Officiallynerdcubed TwITCh: http://www.twitch.tv/nerdcubed TwittEr: https://twitter.com/Dannerdcubed MeRCh! ThinGS: http://www.gametee.co.uk/category/nerdcubed OtHer_Things: https://store.dftba.com/collections/nerdcubed JuNK_Things: https://shop.spreadshirt.co.uk/nerdcubed/\n            "));


            assertEquals(expected, listener.extract(entry));
        }
    }
}