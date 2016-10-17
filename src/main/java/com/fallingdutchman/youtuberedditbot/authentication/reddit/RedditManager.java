package com.fallingdutchman.youtuberedditbot.authentication.reddit;

import com.fallingdutchman.youtuberedditbot.model.RedditCredentials;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import net.dean.jraw.ApiException;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Submission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;

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
    private final AccountManager accountManager;

    /**
     * standard constructor
     */
    public RedditManager(String username) {
        reddit = new RedditClient(UserAgent.of("desktop", "com.fallingdutchman.youtuberedditbot", "1.0", username));
        accountManager = new AccountManager(reddit);
    }

    /**
     * authenticate the user, if {@link #shouldAuth} is set to false this won't do anything
     *
     * @throws NetworkException when the request was not successful
     */
    public void authenticate(RedditCredentials redditCredentials) {
        log.debug("authenticating {} for the reddit api", redditCredentials.getRedditUserName());
        if (!shouldAuth) {
            return;
        }
        final Credentials credentials = Credentials.script(redditCredentials.getRedditUserName(),
                redditCredentials.getRedditPassword(), redditCredentials.getRedditClientId(),
                redditCredentials.getRedditOauthSecret());
        try {
            final OAuthData authData = reddit.getOAuthHelper().easyAuth(credentials);
            reddit.authenticate(authData);
            log.info("successfully authenticated {} for the reddit API",
                    redditCredentials.getRedditUserName());
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
    public Optional<Submission> submitPost(String title, URL url, String subreddit) {
        try {
            log.debug("attempting to submit new post to /r/{}, submission title {}, target url {}",
                    subreddit, title, url.toExternalForm());
            final Submission submission = submitPost(new AccountManager.SubmissionBuilder(url, subreddit, title)
                    .resubmit(false)
                    .sendRepliesToInbox(false));
            log.info("submitted url to /r/{}, submission id: {}", submission.getSubredditName(),
                    submission.getId());

            return Optional.of(submission);
        } catch (ApiException e) {
            log.error(String.format("an API exception occurred whilst trying to submit a post to /r/%s " +
                    "with the title %s and url %s", subreddit, title, url), e);
            return Optional.empty();
        }
    }

    public Optional<Submission> submitSelfPost(String title, String text, String subreddit){
        try {
            log.debug("attempting to submit new self post to /r/{}, submission title {}, body {}",
                    subreddit, title, text);
            final Submission submission = submitPost(new AccountManager.SubmissionBuilder(text, subreddit, title)
                    .resubmit(false)
                    .sendRepliesToInbox(false));
            log.info("submitted self post to /r/{}, submission id: {}", submission.getSubredditName(),
                    submission.getId());

            return Optional.of(submission);
        } catch (ApiException e) {
            log.error(String.format("an API exception occurred whilst trying to submit a post to /r/%s " +
                    "with the title %s and url %s", subreddit, title, text), e);
            return Optional.empty();
        }
    }

    private Submission submitPost(AccountManager.SubmissionBuilder submissionBuilder) throws ApiException {
        return accountManager.submit(submissionBuilder);
    }

    /**
     * submit a comment
     * @param text markdown text for the comment
     * @param submission submission object we want to reply to
     * @return the id of the comment
     */
    public Optional<String> submitComment(String text,Submission submission) {
        try {
            final String commentId = accountManager.reply(submission, text);
            log.info("posted comment to %s on /r/{}, with comment id {}", submission.getId(),
                    submission.getSubredditName(), commentId);

            return Optional.of(commentId);
        } catch (ApiException e) {
            log.error("was unable to post comment", e);
            return Optional.empty();
        }
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
