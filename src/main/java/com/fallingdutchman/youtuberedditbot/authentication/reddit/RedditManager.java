package com.fallingdutchman.youtuberedditbot.authentication.reddit;

import com.fallingdutchman.youtuberedditbot.model.AppConfig;
import com.fallingdutchman.youtuberedditbot.model.Instance;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Synchronized;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.dean.jraw.ApiException;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubmissionKind;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;

import javax.annotation.Nullable;
import java.net.URL;

/**
 * stores a {@link Credentials} object and a {@link RedditClient} for an instance. used to authenticate an user and
 * send requests to reddit.
 *
 * Created by Douwe Koopmans on 28-1-16.
 */
@Slf4j
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedditManager {

    @NonFinal Instance.RedditCredentials redditCredentials;
    AppConfig.RedditConfig redditConfig;
    RedditClient reddit;
    Credentials creds;

    /**
     * standard constructor
     */
    @Inject
    public RedditManager(@Assisted String username, AppConfig config) {
        this.redditConfig = config.getRedditConfig();
        val userAgent = new UserAgent("desktop", redditConfig.getAppId(), redditConfig.getVersion(), username);
        val networkAdapter = new OkHttpNetworkAdapter(userAgent);
        creds = Credentials.script(redditCredentials.getRedditUsername(), redditCredentials.getRedditPassword(),
                redditCredentials.getRedditClientId(), redditCredentials.getRedditOauthSecret());
        this.reddit = OAuthHelper.automatic(networkAdapter, creds);
    }
//
//    /**
//     * authenticate the user, if {@link AppConfig.RedditConfig#authenticatable} is set to false this won't do anything
//     *
//     * @throws NetworkException when the request was not successful
//     */
//    @Synchronized
//    public void authenticate(@NonNull final Instance.RedditCredentials redditCredentials) {
//        if (!redditCredentials.equals(this.redditCredentials)) {
//            this.redditCredentials = redditCredentials;
//        }
//
//        log.debug("authenticating {} for the reddit api", redditCredentials.getRedditUsername());
//
//        try {
//            val authData = reddit.getOAuthHelper().easyAuth(creds);
//            reddit.authenticate(authData);
//            log.info("successfully authenticated {} for the reddit API",
//                    redditCredentials.getRedditUsername());
//        } catch (OAuthException e) {
//            log.error("an OAuth exception occurred whilst trying authenticate "
//                    + redditCredentials.getRedditUsername(), e);
//        } catch (NetworkException e) {
//            log.error("a NetworkException occurred whilst trying to authenticate "
//                    + redditCredentials.getRedditUsername() + '.' + "this could be caused by invalid credentials", e);
//        }
//    }

//    public void reauthenticate() {
//        reddit.getAuthManager().renew();
//    }

    /**
     * submit a link post
     */
    @Synchronized
    @Nullable
    public Submission submitPost(String title, URL url, String subreddit) {
        try {
            log.debug("attempting to submit new post to /r/{}, submission title {}, target url {}",
                    subreddit, title, url.toExternalForm());
            val submission = reddit.subreddit(subreddit).submit(SubmissionKind.LINK, title, url.toExternalForm(), false);
            log.info("submitted url to /r/{}, submission id: {}", submission.inspect().getSubreddit(),
                    submission.getId());

            return submission.inspect();
        } catch (ApiException e) {
            log.error("an API exception occurred whilst trying to submit a post to /r/{} " +
                    "with the title {} and url {}. message: {}", subreddit, title, url, e.getLocalizedMessage());
            log.debug("more info on the API error");
            log.debug("reason: {}", e.getExplanation());
            log.debug("explanation: {}", e.getExplanation());
            log.debug("stacktrace: ", e);
            return null;
        }
    }

    @Synchronized
    @Nullable
    public Submission submitSelfPost(String title, String text, String subreddit) {
        try {
            log.debug("attempting to submit new self post to /r/{}, submission title {}, body {}",
                    subreddit, title, text);
            val submission = reddit.subreddit(subreddit).submit(SubmissionKind.SELF, title, text, false);
            log.info("submitted self post to /r/{}, submission id: {}", submission.inspect().getSubreddit(),
                    submission.getId());

            return submission.inspect();
        } catch (ApiException e) {
            log.error("an API exception occurred whilst trying to submit a post to /r/{} " +
                    "with the title {} and url {}. error message: {}", subreddit, title, text, e.getLocalizedMessage());
            log.debug("more info on the API error");
            log.debug("reason: {}", e.getExplanation());
            log.debug("explanation: {}", e.getExplanation());
            log.debug("stacktrace: ", e);
            return null;
        }
    }

    /**
     * submit a comment
     * @param text markdown text for the comment
     * @param submission submission object we want to reply to
     * @return the id of the comment
     */
    @Synchronized
    @Nullable
    // TODO: 1-7-18 is it more useful to return the commentreference here instead of the ID
    public String submitComment(String text,Submission submission) {

        try {
            val comment = reddit.submission(submission.getId()).reply(text);
            log.info("posted comment to {} on /r/{}, with comment id {}", submission.getId(),
                    submission.getSubreddit(), comment.getId());

            return comment.getId();
        } catch (ApiException e) {
            log.error("an API exception occurred whilst trying to submit a comment to {}. error message: {}" ,
                    submission.getId(), e.getLocalizedMessage());
            log.debug("more info on the API error");
            log.debug("reason: {}", e.getExplanation());
            log.debug("explanation: {}", e.getExplanation());
            log.debug("stacktrace: ", e);
            return null;
        }
    }

}
