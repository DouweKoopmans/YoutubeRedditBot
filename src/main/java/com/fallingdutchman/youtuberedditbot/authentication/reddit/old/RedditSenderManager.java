package com.fallingdutchman.youtuberedditbot.authentication.reddit.old;

import com.fallingdutchman.youtuberedditbot.authentication.reddit.jraw.RedditManager;
import com.fallingdutchman.youtuberedditbot.model.RedditCredentials;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.dean.jraw.http.NetworkException;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Douwe Koopmans on 13-1-16.
 */
public class RedditSenderManager {
    private static final RedditSenderManager ourInstance = new RedditSenderManager();
    private static final String REDDIT_DOMAIN = "http://www.oauth.reddit.com";
    private static final Logger log = LoggerFactory.getLogger(RedditSenderManager.class);

    private RedditUser user;

    private RedditSenderManager() {
    }

    public static RedditSenderManager getInstance() {
        return ourInstance;
    }

    public void setUser(RedditCredentials credentials) {
        RedditManager authenticator = new RedditManager(credentials.getRedditUserName());
        try {
            authenticator.authenticate(credentials);
        } catch (NetworkException e) {
            log.error("unable to authenticate user " + credentials.getRedditUserName(), e);
        }

//        user = new RedditUser()
    }

    public synchronized Response makeRequest(RedditRequest redditRequest, OAuthService oAuthService, Token accessToken){
        final OAuthRequest oAuthRequest = redditRequest.makeRequest();
        oAuthService.signRequest(accessToken, oAuthRequest);
        return oAuthRequest.send();
    }

    public Response postLink(String title, String url, String subreddit, String username, OAuthService oAuthService){
        RedditRequest request = () -> {
            final OAuthRequest oAuthRequest = new OAuthRequest(Verb.POST, REDDIT_DOMAIN + "/api/submit");
            oAuthRequest.addBodyParameter("api_type", "json");
            oAuthRequest.addBodyParameter("title", title);
            oAuthRequest.addBodyParameter("url", url);
            oAuthRequest.addBodyParameter("sr", subreddit);
            oAuthRequest.addBodyParameter("kind", "link");
            oAuthRequest.addBodyParameter("uh", user.getModHash());

            return oAuthRequest;
        };

        return makeRequest(request, oAuthService, new Token(user.getAuthToken(), user.getAuthSecret()));
    }

    public Response postComment(String text, String thingId, String username, OAuthService oAuthService) {
        RedditRequest request = () -> {
            final OAuthRequest oAuthRequest = new OAuthRequest(Verb.POST, REDDIT_DOMAIN + "/api/comment");
            oAuthRequest.addBodyParameter("api_type", "json");
            oAuthRequest.addBodyParameter("text", text);
            oAuthRequest.addBodyParameter("thing_id", thingId);
            oAuthRequest.addBodyParameter("uh", user.getModHash());

            return oAuthRequest;
        };

        return makeRequest(request, oAuthService, new Token(user.getAuthToken(), user.getAuthSecret()));
    }

    public void authenticate(OAuthService service) throws IOException {
        service.getRequestToken();
//        authRegister.add();
    }

    public JsonElement getJsonFromUrl(String url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        InputStreamReader in = new InputStreamReader(con.getInputStream());
        JsonElement jsonElement = new JsonParser().parse(in);
        in.close();
        con.disconnect();
        return jsonElement;
    }
}
