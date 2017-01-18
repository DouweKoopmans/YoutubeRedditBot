package com.fallingdutchman.youtuberedditbot.listeners;

import com.fallingdutchman.youtuberedditbot.YrbUtils;
import com.fallingdutchman.youtuberedditbot.authentication.reddit.RedditManagerRegistry;
import com.fallingdutchman.youtuberedditbot.listeners.filtering.FilterFactory;
import com.fallingdutchman.youtuberedditbot.model.AppConfig;
import com.fallingdutchman.youtuberedditbot.model.Instance;
import com.fallingdutchman.youtuberedditbot.model.YoutubeVideo;
import com.fallingdutchman.youtuberedditbot.processing.ProcessorFactory;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jdom2.Content;
import org.jdom2.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@ToString(doNotUseGetters = true, callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class YoutubeRssFeedListener extends AbstractYoutubeListener<SyndEntry> {

    @Inject
    public YoutubeRssFeedListener(@Assisted Instance instance, ProcessorFactory processorFactory, AppConfig config,
                                  RedditManagerRegistry redditRegistry, FilterFactory filterFactory) throws IOException {
        super(instance, processorFactory, config, redditRegistry, filterFactory);
    }

    @Override
    public YoutubeVideo extract(@NonNull final SyndEntry entry) {
        URL url;
        try {
            url = new URL(entry.getLink());
        } catch (MalformedURLException e) {
            log.error("url on found entry is malformed", e);
            return null;
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
            return null;
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

        return new YoutubeVideo(entry.getTitle(), videoId, url, publishDate, description);
    }

    @Synchronized
    @Override
    public boolean update() {
        try (XmlReader reader = new XmlReader(new URL(getFeedUrl()))) {
            val feed = new SyndFeedInput().build(reader);
            log.trace("updated feed of {}", getInstance().getChannelId());
            setVideos(feed.getEntries().stream()
                    .map(this::extract)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));

            return true;
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
    }

    private String getFeedUrl() {
        return String.format("https://www.youtube.com/feeds/videos.xml?channel_id=%s", getInstance().getChannelId());
    }

}
