package com.fallingdutchman.youtuberedditbot.listeners;

import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManagerFactory;
import com.fallingdutchman.youtuberedditbot.listeners.filtering.FilterFactory;
import com.fallingdutchman.youtuberedditbot.model.AppConfig;
import com.fallingdutchman.youtuberedditbot.model.Instance;
import com.fallingdutchman.youtuberedditbot.model.YoutubeVideo;
import com.fallingdutchman.youtuberedditbot.processing.ProcessorFactory;
import com.google.common.collect.Lists;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.runners.Parameterized.Parameter;
import static org.junit.runners.Parameterized.Parameters;
import static org.mockito.Mockito.mock;

/**
 * Created by Douwe Koopmans on 22-1-16.
 */
@RunWith(Enclosed.class)
public class ListenerTest {

    public static class UnitTests {
        AbstractYoutubeListener<?> listener;

        @Before
        public void setUp() throws Exception {
            ProcessorFactory mockProcessorFactory = mock(ProcessorFactory.class);
            RedditManagerFactory mockRedditFactory = mock(RedditManagerFactory.class);
            FilterFactory filterFactory = mock(FilterFactory.class);
            HistoryManager historyManager = mock(HistoryManager.class);

            Instance instance = new Instance("", new Instance.Comment("", false,
                    Lists.newArrayList()), "", new Instance.RedditCredentials("", "",
                    "", "fake"), Lists.newArrayList(), "", "", 1D,
                    "");

            AppConfig appConfig = new AppConfig(new AppConfig.History("", ""), new AppConfig.Formatting("", ""),
                    new AppConfig.RedditConfig("fake", "fake", false), new AppConfig.UserConfig("", "",
                    "", "", ""), new AppConfig.YoutubeConfig(false, "",
                    0L), new AppConfig.ListenerConfig(0));

            this.listener = new YoutubeRssFeedListener(instance, mockProcessorFactory, appConfig, mockRedditRegistry,
                    filterFactory, historyManager);
        }

        @Test
        public void testScanForNewEntries() throws Exception {

            final LocalDateTime future = LocalDateTime.now().plusMinutes(2);
            final LocalDateTime past = LocalDateTime.now().minusMinutes(2);

            YoutubeVideo entry1 = new YoutubeVideo("", "", new URL("http://www.google.com"), future, "");
            YoutubeVideo entry2 = new YoutubeVideo("", "", new URL("http://www.google.com"), past, "");

            List<YoutubeVideo> entries = Lists.newArrayList(entry1, entry2);

            assertEquals(1, listener.scanForNewEntries(entries));

            entries.remove(entry1);
            entry1 = new YoutubeVideo("", "", new URL("http://www.google.com"), past, "");
            entries.add(entry1);

            assertEquals(0, listener.scanForNewEntries(entries));

        }
    }

    @RunWith(org.junit.runners.Parameterized.class)
    public static class Parameterized {

        YoutubeRssFeedListener listener;

        @Parameter
        public SyndEntry entry;

        @Parameters
        public static Collection<Object> data() throws Exception {
            ClassLoader classLoader = ListenerTest.class.getClassLoader();

            XmlReader reader = new XmlReader(classLoader.getResource("test-RssEntry.xml"));

            return new ArrayList<>(new SyndFeedInput().build(reader).getEntries());
        }

        @Before
        public void setUp() throws Exception {
            ProcessorFactory mockProcessorFactory = mock(ProcessorFactory.class);
            RedditManagerFactory mockRedditFactory = mock(RedditManagerFactory.class);
            FilterFactory filterFactory = mock(FilterFactory.class);
            HistoryManager mockHistoryManager = mock(HistoryManager.class);

            Instance instance = new Instance("", new Instance.Comment("", false,
                    Lists.newArrayList()), "", new Instance.RedditCredentials("", "",
                    "", "fake"), Lists.newArrayList(), "", "", 1D,
                    "");

            AppConfig appConfig = new AppConfig(new AppConfig.History("", ""), new AppConfig.Formatting("", ""),
                    new AppConfig.RedditConfig("fake", "fake", false), new AppConfig.UserConfig("", "",
                    "", "", ""), new AppConfig.YoutubeConfig(false, "",
                    0L), new AppConfig.ListenerConfig(0));

            this.listener = new YoutubeRssFeedListener(instance, mockProcessorFactory, appConfig,
                    mockRedditRegistry, filterFactory, mockHistoryManager);
        }

        @Test
        public void testRssExtract() throws Exception {
            YoutubeVideo expected = new YoutubeVideo("Nerd³ Completes... Watch Dogs 2 - 18 - Marcus Kart", "4iwPS-L3dJc",
                    new URL("http://www.youtube.com/watch?v=4iwPS-L3dJc"),
                    LocalDateTime.of(2017, 1, 11, 20, 0, 0), "\n                HACK_THE_PLANET GaMe_LINk: https://www.ubisoft.com/en-US/game/watch-dogs-2 NERd³_SiTE http://nerdcubed.co.uk NeRd³_PAtrEon: https://www.patreon.com/nerdcubed DAd³_ChaNNeL: http://www.youtube.com/user/OfficialDadCubed TOy_ChanNEL: http://www.youtube.com/user/Officiallynerdcubed TwITCh: http://www.twitch.tv/nerdcubed TwittEr: https://twitter.com/Dannerdcubed MeRCh! ThinGS: http://www.gametee.co.uk/category/nerdcubed OtHer_Things: https://store.dftba.com/collections/nerdcubed JuNK_Things: https://shop.spreadshirt.co.uk/nerdcubed/\n            ");


            assertEquals(expected, listener.extract(entry));
        }
    }
}