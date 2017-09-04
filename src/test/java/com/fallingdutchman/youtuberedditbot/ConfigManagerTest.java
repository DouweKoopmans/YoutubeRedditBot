package com.fallingdutchman.youtuberedditbot;

import com.fallingdutchman.youtuberedditbot.model.Instance;
import com.fallingdutchman.youtuberedditbot.model.Instance.Comment;
import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by douwe on 11-1-17.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigManagerTest {
    @Mock
    private ConfigManager mockConfigManager;

    @Test
    public void loadConfig() throws Exception {
        List<Instance> expected = Lists.newArrayList(
                new Instance(true, "fakeChannel", "fakeChannel", new Comment("description",
                        true, Lists.newArrayList(new Comment.CommentRule("foo", "bar"))),
                        "new-video", new Instance.RedditCredentials("fake", "fake",
                        "fake", "fake"), Lists.newArrayList("foobar"), "fake", null,
                        1, "api"),
                new Instance(true, "fakeChannel", "channel1", new Comment("description",
                        true, Lists.newArrayList(new Comment.CommentRule("foo", "bar"))),
                        "description-mention", new Instance.RedditCredentials("fake", "fake",
                        "fake", "fake"), Lists.newArrayList("foobar"), "fake", new Instance.Target("name", "channel2"),
                        1, "api")
        );

        final Config config = ConfigManager.prepareConfig(ConfigFactory.parseResources("test-bots.conf"));
        //noinspection AccessStaticViaInstance
        final List<Instance> actual = mockConfigManager.getInstancesFromConfig(config);
        assertEquals("instances were incorrectly loaded", expected, actual);
    }
}