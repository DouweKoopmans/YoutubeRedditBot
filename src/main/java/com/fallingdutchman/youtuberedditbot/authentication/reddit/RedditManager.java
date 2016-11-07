package com.fallingdutchman.youtuberedditbot.authentication.reddit;

import com.fallingdutchman.youtuberedditbot.model.RedditCredentials;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.dean.jraw.ApiException;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Submission;

import java.net.URL;
import java.util.Optional;

/**
 * stores a {@link Credentials} object and a {@link RedditClient} for an instance. used to authenticate an user and
 * send requests to reddit.
 *
 * Created by Douwe Koopmans on 28-1-16.
 */
@Slf4j
@ToString
@EqualsAndHashCode
public class RedditManager {
    private final RedditClient reddit;

    /**
     * whether the {@link #authenticate(RedditCredentials)} method should execute, set to false for testing purposes
     */
    public boolean shouldAuth = true;
    private final AccountManager accountManager;
    private RedditCredentials redditCredentials;

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
    public void authenticate(final RedditCredentials redditCredentials) {
        this.redditCredentials = redditCredentials;
        if (!shouldAuth) {
            return;
        }
        log.debug("authenticating {} for the reddit api", redditCredentials.getRedditUserName());
        val credentials = Credentials.script(redditCredentials.getRedditUserName(),
                redditCredentials.getRedditPassword(), redditCredentials.getRedditClientId(),
                redditCredentials.getRedditOauthSecret());
        try {
            val authData = reddit.getOAuthHelper().easyAuth(credentials);
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

    public void reauthenticate() {
        this.authenticate(redditCredentials);
    }

    /**
     * submit a link post
     */
    public Optional<Submission> submitPost(String title, URL url, String subreddit) {
        try {
            log.debug("attempting to submit new post to /r/{}, submission title {}, target url {}",
                    subreddit, title, url.toExternalForm());
            val submission = submitPost(new AccountManager.SubmissionBuilder(url, subreddit, title)
                    .resubmit(false)
                    .sendRepliesToInbox(false));
            log.info("submitted url to /r/{}, submission id: {}", submission.getSubredditName(),
                    submission.getId());

            return Optional.of(submission);
        } catch (ApiException e) {
            log.error("an API exception occurred whilst trying to submit a post to /r/{} " +
                    "with the title {} and url {}", subreddit, title, url, e);
            return Optional.empty();
        }
    }

    public Optional<Submission> submitSelfPost(String title, String text, String subreddit) {
        try {
            log.debug("attempting to submit new self post to /r/{}, submission title {}, body {}",
                    subreddit, title, text);
            val submission = submitPost(new AccountManager.SubmissionBuilder(text, subreddit, title)
                    .resubmit(false)
                    .sendRepliesToInbox(false));
            log.info("submitted self post to /r/{}, submission id: {}", submission.getSubredditName(),
                    submission.getId());

            return Optional.of(submission);
        } catch (ApiException e) {
            log.error("an API exception occurred whilst trying to submit a post to /r/{} " +
                    "with the title {} and url {}", subreddit, title, text, e);
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
            val commentId = accountManager.reply(submission, text);
            log.info("posted comment to {} on /r/{}, with comment id {}", submission.getId(),
                    submission.getSubredditName(), commentId);

            return Optional.of(commentId);
        } catch (ApiException e) {
            log.error("was unable to post comment", e);
            return Optional.empty();
        }
    }
}
