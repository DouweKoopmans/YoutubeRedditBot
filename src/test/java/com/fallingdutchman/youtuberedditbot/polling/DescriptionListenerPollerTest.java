package com.fallingdutchman.youtuberedditbot.polling;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by douwe on 7-10-16.
 */
@RunWith(value = Parameterized.class)
public class DescriptionListenerPollerTest {

    private String singleLineDescription;
    private static final String channelName = "test123";
    private static final String channelId = "foobar";

    public DescriptionListenerPollerTest(String SingleLineDescription) {
        this.singleLineDescription = SingleLineDescription;
    }

    @Test
    public void checkEntrySingleLine() throws Exception {
        final String description = String.format("https://www.youtube.com/%s", singleLineDescription);
        Assert.assertTrue("did not detect simple singleLineDescription",
                DescriptionListenerPoller.checkEntry(description, channelName, channelId));
    }

    @Test
    public void checkEntryMultiline() throws Exception {
        final String description = String.format("test123\n\n foo %s bar\ntest",
                "https://www.youtube.com/" + singleLineDescription);

        Assert.assertTrue(DescriptionListenerPoller.checkEntry(description, channelName, channelId));
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