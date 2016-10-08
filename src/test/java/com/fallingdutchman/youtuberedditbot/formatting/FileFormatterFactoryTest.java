package com.fallingdutchman.youtuberedditbot.formatting;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by douwe on 3-10-16.
 */
public class FileFormatterFactoryTest {
    @Test
    public void generateFileLocationSimple() throws Exception {
        final String testInput = "foo";

        assertEquals("generated file location for " + testInput + "was incorrect",
                "data/formats/" + testInput + ".md", FileFormatterFactory.generateFileLocation(testInput));
    }

    @Test(expected=NullPointerException.class)
    public void generateFileLocationNull() throws Exception {
        final String testInput = null;

        FileFormatterFactory.generateFileLocation(testInput);
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateFileLocationEmpty() throws Exception {
        final String testInput = "";

        FileFormatterFactory.generateFileLocation(testInput);
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateFileLocationSuffix() throws Exception {
        final String testInput = "foobar.";

        FileFormatterFactory.generateFileLocation(testInput);
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateFileLocationPrefix() throws Exception {
        final String testInput = ".foobar";

        FileFormatterFactory.generateFileLocation(testInput);
    }

    @Test
    public void generateFileLocationAdvanced() throws Exception {
        final String testInput = "FOO/BAR";

        assertEquals("input string was not correctly clean", "data/formats/foobar.md",
                FileFormatterFactory.generateFileLocation(testInput));
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