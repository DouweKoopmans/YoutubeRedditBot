package com.fallingdutchman.youtuberedditbot;

import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManager;
import com.fallingdutchman.youtuberedditbot.config.ConfigHandler;
import com.fallingdutchman.youtuberedditbot.model.Instance;
import com.fallingdutchman.youtuberedditbot.polling.AbstractPoller;
import com.fallingdutchman.youtuberedditbot.polling.DefaultNewVideoPoller;
import com.fallingdutchman.youtuberedditbot.polling.DescriptionListenerPoller;
import com.fallingdutchman.youtuberedditbot.processing.YoutubeProcessor;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.dean.jraw.models.Submission;
import org.jdom2.Content;
import org.jdom2.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Created by Douwe Koopmans on 8-1-16.
 */
@Slf4j
@ToString(exclude = {"feed", "timer", "authenticator", "poller"}, doNotUseGetters = true)
@EqualsAndHashCode(exclude = {"feed", "timer"})
public final class YoutubeFeedListener implements FeedListener {
    private final RedditManager authenticator;
    private final Instance instance;
    private final AtomicReference<SyndFeed> feed = new AtomicReference<>();
    private final AbstractPoller poller;
    private LocalDateTime latestVideo = LocalDateTime.now();
    private Timer timer;

    private YoutubeFeedListener(RedditManager authenticator, Instance instance) throws IOException {
        Preconditions.checkNotNull(authenticator);
        Preconditions.checkNotNull(instance);

        this.authenticator = authenticator;
        this.instance = instance;
        this.authenticator.authenticate(ConfigHandler.getInstance().getRedditCredentials());

        poller = createPoller();
    }

    public static YoutubeFeedListener of(Instance instance, String username) throws IOException {
        return new YoutubeFeedListener(new RedditManager(username), instance);
    }

    public static YoutubeFeedListener of(Instance instance, RedditManager authenticator) throws IOException {
        return new YoutubeFeedListener(authenticator, instance);
    }

    @Override
    public final void listen() {
        log.info("starting up new listener for " + instance.getChannelId());
        timer = new Timer();

        try {
            if (updateFeed()) {
                final Optional<YoutubeVideo> youtubeVideo = this.find(feed.get().getEntries().get(0));
                if (youtubeVideo.isPresent()) {
                    this.setLatestVideo(youtubeVideo.get().getPublishDate());
                    timer.schedule(getPoller(), 0, 30000);
                } else {
                    log.warn(String.format("an error occurred whilst trying to start the listener, will not start" +
                            " following listener %s", this));
                }
            } else {
                log.warn("was unable to initiate the feed will not start the poller for " + this.toString());
            }
        } catch (Exception e) {
            log.error("an error occurred whilst trying to Listen to the feed: ", e);
        }
    }

    @Override
    public final void stopListening() {
        log.info("stopping listener for " + instance.getChannelId());
        timer.cancel();
    }

    public AbstractPoller getPoller() {
        return poller;
    }

    public Instance getInstance() {
        return instance;
    }

    private SyndFeed getFeed() {
        return feed.get();
    }

    public List<SyndEntry> getFeedEntries() {
        return ImmutableList.copyOf(getFeed().getEntries());
    }

    public LocalDateTime getLatestVideo() {
        return latestVideo;
    }

    protected void setLatestVideo(LocalDateTime date) {
        this.latestVideo = date;
        log.debug("setting latest video date of {} to {}", this.instance.getChannelId(), date);
    }

    public Optional<YoutubeVideo> find(SyndEntry entry) {
        URL url;
        try {
            url = new URL(entry.getLink());
        } catch (MalformedURLException e) {
            log.error("url on found entry is malformed", e);
            return Optional.empty();
        }
        String videoId;
        String description = "";
        final LocalDateTime publishDate = YrbUtils.dateToLocalDate(entry.getPublishedDate());

        // get the video id
        Optional<Element> optionalVideoId = entry.getForeignMarkup().stream()
                .filter(element -> "videoId".equals(element.getName()))
                .findFirst();

        if (optionalVideoId.isPresent()) {
            videoId = optionalVideoId.get().getValue();
        } else {
            log.error("was not able to find a videoId in feed entry " + entry.toString());
            return Optional.empty();
        }

        // find description
        final Optional<String> des = entry.getForeignMarkup().stream()
                .filter(element -> "media".equals(element.getNamespacePrefix()) && "group".equals(element.getName()))
                .map(Element::getContent)
                .flatMap(Collection::stream)
                .filter(content -> content.getCType().equals(Content.CType.Element))
                .map(content -> (Element) content)
                .filter(element -> "description".equalsIgnoreCase(element.getName()))
                .map(Element::getContent)
                .flatMap(Collection::stream)
                .map(Content::getValue)
                .findFirst();

        if (des.isPresent()) {
            description = des.get();
        }

        return Optional.of(new YoutubeVideo(entry.getTitle(), videoId, url, description, publishDate,
                instance.getChannelId()));
    }

    public void newVideoPosted(YoutubeVideo video) {
        log.info("found a new video, \n" + video.toString());
        this.setLatestVideo(video.getPublishDate());
        YoutubeProcessor processor = new YoutubeProcessor(video, authenticator);

        log.info("processing video, id=\"{}\"", video.getVideoId());
        getInstance().getSubreddits().forEach(processVideo(processor));
    }

    private Consumer<String> processVideo(YoutubeProcessor processor) {
        return subreddit -> {
            log.debug("processing new video for /r/{}", subreddit);
            final Optional<Submission> submission = processor.postVideo(subreddit, false,
                    () -> this.authenticator.authenticate(ConfigHandler.getInstance().getRedditCredentials()));
            if (submission.isPresent() && instance.shouldPostDescription()) {
                processor.postComment(submission.get(), "description");
            }
        };
    }

    public boolean updateFeed() {
        try (XmlReader reader = new XmlReader(new URL(generateFeedUrlFromId(instance.getChannelId())))) {
            this.feed.set(new SyndFeedInput().build(reader));
            log.trace("updated feed of {}", instance.getChannelId());
        } catch (FeedException e) {
            log.error("was unable to parse feed", e);
            return false;
        } catch (MalformedURLException e) {
            log.error(String.format("youtube feed URL is malformed, please check the configurations " +
                    "for channel-id %s", instance.getChannelId()), e);
            return false;
        } catch (IOException e) {
            log.error("an error occurred whilst trying to read the stream of the provided youtube-feed", e);
            return false;
        }
        return true;
    }

    private String generateFeedUrlFromId(final String channelId) {
        return String.format("https://www.youtube.com/feeds/videos.xml?channel_id=%s", channelId);
    }

    private AbstractPoller createPoller() {
        switch (instance.getType()) {
            case "descriptionListener":
                return new DescriptionListenerPoller(this);
            case "newVideoListener":
            default:
                return new DefaultNewVideoPoller(this);
        }
    }

}
