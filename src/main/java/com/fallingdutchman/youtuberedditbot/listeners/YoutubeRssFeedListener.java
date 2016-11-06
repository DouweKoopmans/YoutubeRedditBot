package com.fallingdutchman.youtuberedditbot.listeners;

import com.fallingdutchman.youtuberedditbot.YrbUtils;
import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManager;
import com.fallingdutchman.youtuberedditbot.model.Instance;
import com.fallingdutchman.youtuberedditbot.model.YoutubeVideo;
import com.google.common.collect.ImmutableList;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jdom2.Content;
import org.jdom2.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Created by Douwe Koopmans on 8-1-16.
 */
@Slf4j
@ToString(exclude = {"feed"}, doNotUseGetters = true, callSuper = true)
@EqualsAndHashCode(exclude = "feed", callSuper = true)
public final class YoutubeRssFeedListener extends AbstractYoutubeListener<SyndEntry> {
    private final AtomicReference<SyndFeed> feed = new AtomicReference<>();

    public YoutubeRssFeedListener(RedditManager authenticator, Instance instance) throws IOException {
       super(authenticator, instance);
    }

    @Override
    boolean onListen() {
        val youtubeVideo = this.extract(feed.get().getEntries().get(0));
        if (youtubeVideo.isPresent()) {
            this.setLatestVideo(youtubeVideo.get().getPublishDate());
            return true;
        } else {
            log.warn("was unable to find latest video because of an issue with the feed, will not start the" +
                    " following listener {}", this);
            return false;
        }
    }

    public Optional<YoutubeVideo> extract(@NonNull final SyndEntry entry) {
        URL url;
        try {
            url = new URL(entry.getLink());
        } catch (MalformedURLException e) {
            log.error("url on found entry is malformed", e);
            return Optional.empty();
        }
        String videoId;
        String description = "";
        val publishDate = YrbUtils.dateToLocalDateTime(entry.getPublishedDate());

        // get the video id
        Optional<Element> optionalVideoId = entry.getForeignMarkup().stream()
                .filter(element -> "videoId".equals(element.getName()))
                .findFirst();

        if (optionalVideoId.isPresent()) {
            videoId = optionalVideoId.get().getValue();
        } else {
            log.error("was not able to extract a videoId in feed entry {}", entry.toString());
            return Optional.empty();
        }

        // extract description
        val des = entry.getForeignMarkup().stream()
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

        return Optional.of(new YoutubeVideo(entry.getTitle(), videoId, url, publishDate, description));
    }

    @Override
    public boolean update() {
        try (XmlReader reader = new XmlReader(new URL(getFeedUrl()))) {
            this.feed.set(new SyndFeedInput().build(reader));
            log.trace("updated feed of {}", getInstance().getChannelId());
        } catch (FeedException e) {
            log.error("was unable to parse feed", e);
            return false;
        } catch (MalformedURLException e) {
            log.error("youtube feed URL is malformed, please check the configurations for channel-id {}",
                    getInstance().getChannelId(), e);
            return false;
        } catch (IOException e) {
            log.error("an error occurred whilst trying to read the stream of the provided youtube-feed", e);
            return false;
        }
        return true;
    }

    @Override
    public List<YoutubeVideo> getVideos() {
        return ImmutableList.copyOf(getFeed().getEntries()
                .stream()
                .map(syndEntry -> {
                    Optional<YoutubeVideo> video = extract(syndEntry);
                    if (video.isPresent()) {
                        return video.get();
                    } else {
                        throw new IllegalStateException("there are malformed entries in the feed");
                    }
                })
                .collect(Collectors.toList()));
    }

    private SyndFeed getFeed() {
        return feed.get();
    }

    private String getFeedUrl() {
        return String.format("https://www.youtube.com/feeds/videos.xml?channel_id=%s", getInstance().getChannelId());
    }

}
