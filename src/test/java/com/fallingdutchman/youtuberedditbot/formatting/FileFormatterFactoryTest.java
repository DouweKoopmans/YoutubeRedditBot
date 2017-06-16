package com.fallingdutchman.youtuberedditbot.formatting;

import com.fallingdutchman.youtuberedditbot.model.AppConfig;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by douwe on 3-10-16.
 */
public class FileFormatterFactoryTest {
    private FileFormatterFactory formatterFactory;

    @Before
    public void setUp() throws Exception {
        formatterFactory = new FileFormatterFactory(new AppConfig(new AppConfig.History("", ""), new AppConfig.Formatting("md", "data/formats/"),
                new AppConfig.RedditConfig("fake", "fake", false), new AppConfig.UserConfig("data/", "",
                "", "", ""), new AppConfig.YoutubeConfig(false, "",
                0L), new AppConfig.ListenerConfig(0)));
    }

    @Test
    public void generateFileLocationSimple() throws Exception {
        final String testInput = "foo";

        assertEquals("generated file location for " + testInput + " was incorrect",
                "data/formats/" + testInput + ".md", formatterFactory.generateFileLocation(testInput));
    }

    @Test(expected=NullPointerException.class)
    public void generateFileLocationNull() throws Exception {
        final String testInput = null;

        formatterFactory.generateFileLocation(testInput);
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateFileLocationEmpty() throws Exception {
        final String testInput = "";

        formatterFactory.generateFileLocation(testInput);
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateFileLocationSuffix() throws Exception {
        final String testInput = "foobar.";

        formatterFactory.generateFileLocation(testInput);
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateFileLocationPrefix() throws Exception {
        final String testInput = ".foobar";

        formatterFactory.generateFileLocation(testInput);
    }

    @Test
    public void generateFileLocationAdvanced() throws Exception {
        final String testInput = "FOO/BAR";

        assertEquals("input string was not correctly clean", "data/formats/foo/bar.md",
                formatterFactory.generateFileLocation(testInput));
    }

    @Test
    public void joinStringsSimple() throws Exception {
        final List<String> testInput = Lists.newArrayList("foo", "bar");

        final String actual = FileFormatterFactory.joinStrings(testInput);
        assertEquals("incorrectly joined list of strings", "foo\nbar", actual);
    }

    @Test(expected = NullPointerException.class)
    public void joinStringsNull() throws Exception {
        final List<String> testInput = null;

        FileFormatterFactory.joinStrings(testInput);
    }

    @Test
    public void joinStringsAdvanced() throws Exception {
        final List<String> testInput = Lists.newArrayList("foo\n", "bar\n");

        final String actual = FileFormatterFactory.joinStrings(testInput);
        assertEquals("foo\n\nbar\n", actual);
    }

    @Test
    public void joinStringsSingleEntry() throws Exception {
        final List<String> testInput = Lists.newArrayList("foo");

        final String actual = FileFormatterFactory.joinStrings(testInput);
        assertEquals("incorrectly handle input with a single entry", "foo", actual);
    }

    @Test
    public void joinStringsNoEntry() throws Exception {
        final List<String> testInput = Lists.newArrayList();

        final String actual = FileFormatterFactory.joinStrings(testInput);
        assertEquals("incorrectly handle input with a single entry", "", actual);
    }
}