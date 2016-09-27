package com.fallingdutchman.youtuberedditbot.authentication.reddit.jraw;

import com.fallingdutchman.youtuberedditbot.config.model.RedditCredentials;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.HttpRequest;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.RestResponse;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * stores a {@link Credentials} object and a {@link RedditClient} for an instance. used to authenticate an user and
 * send requests to reddit.
 *
 * Created by Douwe Koopmans on 28-1-16.
 */
public class RedditManager {
    private static final Logger log = LoggerFactory.getLogger(RedditManager.class);


    private final RedditClient reddit;

    /**
     * whether the {@link #authenticate(RedditCredentials)} method should execute, set to false for testing purposes
     */
    public boolean shouldAuth = true;

    /**
     * standard constructor
     */
    public RedditManager(String username) {
        reddit = new RedditClient(UserAgent.of("desktop", "com.fallingdutchman.YoutubeRedditBot", "1.0", username));
    }

    /**
     * authenticate the user, if {@link #shouldAuth} is set to false this won't do anything
     *
     * @throws NetworkException when the request was not successful
     */
    public void authenticate(RedditCredentials redditCredentials) {
        if (!shouldAuth) {
            return;
        }
        final Credentials credentials = Credentials.script(redditCredentials.getRedditUserName(),
                redditCredentials.getRedditPassword(), redditCredentials.getRedditClientId(),
                redditCredentials.getRedditOauthSecret());

        try {
            reddit.authenticate(reddit.getOAuthHelper().easyAuth(credentials));
        } catch (OAuthException e) {
            log.error("an OAuth exception occurred whilst trying authenticate "
                    + redditCredentials.getRedditUserName(), e);
        } catch (NetworkException e) {
            log.error("a NetworkException occurred whilst trying to authenticate "
                    + redditCredentials.getRedditUserName() + '.' + "this could be caused by invalid credentials", e);
        }
    }

    /**
     * submit a link post
     */
    public RestResponse submitPost(String title, String url, String subreddit) {
        Map<String, String> postArgs = Maps.newLinkedHashMap();
        postArgs.put("title", title);
        postArgs.put("url", url);
        postArgs.put("sr", subreddit);
        postArgs.put("kind", "link");
        postArgs.put("uh", reddit.me().data("modhash"));

        HttpRequest httpRequest = null;
        try {
            httpRequest = HttpRequest.Builder
                    .from("POST", new URL("http://www.reddit.com/api/submit"))
                    .post(postArgs)
                    .build();

        } catch (MalformedURLException ignored) {
        }

        return reddit.execute(httpRequest);
    }

    /**
     * submit a comment
     * @param text markdown text for the comment
     * @param parentFullname fullname of the parent thing this comment needs to point to (https://www.reddit.com/dev/api#fullnames)
     * @return the response
     */
    public RestResponse submitComment(String text,String parentFullname) {
        Map<String, String> postArgs = Maps.newLinkedHashMap();
        postArgs.put("api_type", "json");
        postArgs.put("text", text);
        postArgs.put("thing_id", parentFullname);
        postArgs.put("uh", reddit.me().data("modhash"));

        HttpRequest httpRequest = null;
        try {
            httpRequest = HttpRequest.Builder
                    .from("POST", new URL("https://www.reddit.com/api/comment"))
                    .post(postArgs)
                    .build();
        } catch (MalformedURLException ignored) {
        }

        return reddit.execute(httpRequest);
    }

    /**
     * send a request to reddit, used to make sure requests are send in sequence
     * @param client the reddit client of a logged in user
     * @param request the request to be send
     * @return the response message from the server
     */
    public static synchronized RestResponse makeRequest(RedditClient client, HttpRequest request) {
        return client.execute(request);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("reddit", reddit)
                .add("shouldAuth", shouldAuth)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RedditManager)) return false;
        RedditManager that = (RedditManager) o;
        return shouldAuth == that.shouldAuth &&
                Objects.equal(reddit, that.reddit);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(reddit, shouldAuth);
    }
}
