package com.fallingdutchman.youtuberedditbot.listeners;

import com.fallingdutchman.youtuberedditbot.YoutubeVideo;
import com.fallingdutchman.youtuberedditbot.YrbUtils;
import com.fallingdutchman.youtuberedditbot.authentication.reddit.jraw.RedditManager;
import com.fallingdutchman.youtuberedditbot.config.model.Instance;
import com.fallingdutchman.youtuberedditbot.feedregister.FeedRegister;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.jdom2.Content;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Douwe Koopmans on 8-1-16.
 */
// TODO: 8-1-16 add some log messages to this
public final class FeedListener implements IFeedListener{
    private static final Logger log = LoggerFactory.getLogger(FeedListener.class);
    private final String channelId;
    @VisibleForTesting
    final RedditManager authenticator;
    private LocalDateTime latestVideo = LocalDateTime.now();
    private final Instance configInstance;
    private SyndFeed feed;
    private Timer timer;
    private final AbstractPoller poller;

    private FeedListener(RedditManager authenticator, Instance configInstance)
            throws IOException, FeedException {
        this.authenticator = authenticator;
        this.configInstance = configInstance;

//        authenticator.authenticate(ConfigHandler.getInstance().getRedditCredentials());

        String feedUrl = configInstance.getYoutubeFeed();
        Matcher matcher = Pattern.compile(".*channel_id=([\\w\\-]*)").matcher(feedUrl);
        if (matcher.find()) {
            this.channelId = matcher.group(1);
        } else {
            this.channelId = feedUrl;
        }

        poller = createPoller();
    }

    public static FeedListener of(Instance instance, String username) throws IOException, FeedException {
        return new FeedListener(new RedditManager(username), instance);
    }

    public static FeedListener of(Instance instance, RedditManager authenticator)
            throws IOException, FeedException{
        return new FeedListener(authenticator, instance);
    }

    /**
     * generate a string with markdown for the description to be posted to reddit
     * @param description the original description
     * @return the generated string
     */
    public static String generateMdDescription(String description) {
        return "#Description:\n" + description.replace("\n", "  \n");
    }

    @Override
    public final void listen() {
        log.info("starting up new listener for " + this.channelId);
        timer = new Timer();

        try {
            updateFeed();

            setLatestVideo(this.find(feed.getEntries().get(0)).getPublishDate());
            timer.schedule(getPoller(), 0, 30000);
        } catch (Exception e) {
            log.error("an error occurred whilst trying to Listen to the feed: ", e);
        }
    }

    @Override
    public final void stopListening(){
        log.info("stopping listener for " + this.getChannelId());
        timer.cancel();
    }

    @Override
    public String getChannelId() {
        return channelId;
    }

    public AbstractPoller getPoller() {
        return poller;
    }

    protected Instance getConfigInstance() {
        return configInstance;
    }

    protected SyndFeed getFeed(){
        return feed;
    }

    protected LocalDateTime getLatestVideo() {
        return latestVideo;
    }

    protected void setLatestVideo(LocalDateTime date) {
        this.latestVideo = date;

        log.debug("setting latestVideo Date for ID " + FeedRegister.getInstance().keyOf(this)
                + " to: " + date );
    }

    protected YoutubeVideo find(SyndEntry entry) {
        URL url = null;
        try {
            url = new URL(entry.getLink());
        } catch (MalformedURLException e) {
            log.error("url on found entry is malformed", e);
        }
        String videoId = null;
        String description = "";
        final String videoTitle = entry.getTitle();
        final LocalDateTime publishDate = YrbUtils.dateToLocalDate(entry.getPublishedDate());

        // get the video id
        Optional<Element> optionalVideoId = entry.getForeignMarkup().stream()
                .filter(element -> "videoId".equals(element.getName()))
                .findFirst();

        if (optionalVideoId.isPresent()) {
            videoId = optionalVideoId.get().getValue();
        }

        // get a list of all child elements of the "media:group" element
        List<Element> mediaElements = entry.getForeignMarkup().stream()
                .filter(element -> "media".equals(element.getNamespacePrefix()) && "group".equals(element.getName()))
                .collect(Collectors.toList());

        final Optional<String> des = mediaElements.stream()
                .filter(element -> element.getName().equalsIgnoreCase("group"))
                .map(Element::getContent)
                .flatMap(Collection::stream)
                .filter(content -> content.getCType().equals(Content.CType.Element))
                .map(content -> (Element) content)
                .filter(element -> element.getName().equalsIgnoreCase("description"))
                .map(Element::getContent)
                .flatMap(Collection::stream)
                .map(Content::getValue)
                .findFirst();

        if (des.isPresent()) {
            description = des.get();
        }

        return new YoutubeVideo(videoTitle, videoId, url, description, publishDate, this.channelId);
    }

    protected void newVideoPosted(YoutubeVideo video) {
        log.info("found a new video, " + video.toString());
        this.setLatestVideo(video.getPublishDate());

//        new Thread(() -> getConfigInstance().getSubreddits()
//                .forEach(subreddit -> getPoller().processNewVideo(video, subreddit))).start();
    }

    // TODO: 5-2-2016 remove throw declaration in favor of a local try-catch chain to reduce duplicate code
    protected void updateFeed() throws IOException, FeedException {
        synchronized (FeedListener.class) {
            SyndFeedInput input = new SyndFeedInput();
            XmlReader reader = new XmlReader(new URL(configInstance.getYoutubeFeed()));
            this.feed = input.build(reader);
            reader.close();
        }
    }

    private AbstractPoller createPoller() {
        switch (configInstance.getType()){
            case "descriptionListener":
                return new DescriptionListenerPoller(this);
            case "newVideoListener":
            default:
                return new DefaultNewVideoPoller(this);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("channelId", channelId)
                .add("latestVideo", latestVideo)
                .add("configInstance", configInstance)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FeedListener)) {
            return false;
        }
        FeedListener that = (FeedListener) o;
        return Objects.equal(getChannelId(), that.getChannelId()) &&
                Objects.equal(timer, that.timer) &&
                Objects.equal(getLatestVideo(), that.getLatestVideo()) &&
                Objects.equal(configInstance, that.configInstance) &&
                Objects.equal(getFeed(), that.getFeed());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getChannelId(), timer, getLatestVideo(), configInstance, getFeed());
    }

    @Override
    public void print() {
        log.info("ChannelId:  " + getChannelId());
        log.info("LatestVideo: " + getLatestVideo());
        getConfigInstance().print();
    }
}
