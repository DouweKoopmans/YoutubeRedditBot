package com.fallingdutchman.youtuberedditbot.formatting;

import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by douwe on 4-11-16.
 */
@RunWith(Parameterized.class)
public class FormatterTest {

    @Parameterized.Parameter public String format;
    @Parameterized.Parameter(1) public String input;
    @Parameterized.Parameter(2) public String expected;
    private Formatter formatter;

    @Before
    public void setup() {
         formatter = new Formatter(format);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                        {"${test}", "foo bar\n", "foo bar\n"},
                        {"***\n${test}\n***", "foo bar", "***\nfoo bar\n***"},
                        {"***\n\n***", "foo bar", "***\n\n***"},
                        {"***foo***", "bar", "***foo***"}
                }
        );
    }

    @Test
    public void format() throws Exception {
        final HashMap<String, String> values = Maps.newHashMap();
        values.put("test", input);

        Assert.assertEquals(expected, formatter.format(values));
    }

}