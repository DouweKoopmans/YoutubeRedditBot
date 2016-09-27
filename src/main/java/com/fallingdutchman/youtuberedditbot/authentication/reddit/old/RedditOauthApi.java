package com.fallingdutchman.youtuberedditbot.authentication.reddit.old;

import com.fallingdutchman.youtuberedditbot.config.ConfigHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.scribe.builder.api.DefaultApi20;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.model.*;
import org.scribe.oauth.OAuth20ServiceImpl;
import org.scribe.oauth.OAuthService;

/**
 * Created by Douwe Koopmans on 13-1-16.
 */
public class RedditOauthApi extends DefaultApi20 {

    @Override
    public String getAccessTokenEndpoint() {
        return "https://www.reddit.com/api/v1/access_token";
    }

    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        return String.format("https://www.reddit.com/api/v1/authorize?client_id=%s&response_type=code&" +
                "redirect_uri=%s&scope=%s", config.getApiKey(),config.getCallback(), config.getScope());
    }

    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }

    @Override
    public AccessTokenExtractor getAccessTokenExtractor() {
        return response -> {
            final String oauthSecret = ConfigHandler.getInstance().getRedditCredentials().getRedditOauthSecret();

            final JsonObject responseJson = new JsonParser().parse(response).getAsJsonObject();
            return new Token(responseJson.get("access_token").getAsString(), oauthSecret , response);
        };
    }

    @Override
    public OAuthService createService(OAuthConfig config) {
        return new RedditOauth2Service(this, config);
    }

    // TODO: 20-9-16
    private class RedditOauth2Service extends OAuth20ServiceImpl {
        private static final String GRANT_TYPE = ""; // TODO: 20-9-16
        private final DefaultApi20 api;
        private final OAuthConfig config;

        RedditOauth2Service(DefaultApi20 api, OAuthConfig config) {
            super(api, config);
            this.api = api;
            this.config = config;
        }

        @Override
        public Token getAccessToken(Token requestToken, Verifier verifier) {
            OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());

            switch (api.getAccessTokenVerb()) {
                case POST:
                    createPostRequest(verifier, request);

                    break;
                case GET:
                default:
                    createGetRequest(verifier, request);

                    if (config.hasScope()) {
                        request.addQuerystringParameter(OAuthConstants.SCOPE, config.getScope());
                    }
            }
            Response response = request.send();
            return api.getAccessTokenExtractor().extract(response.getBody());
        }

        @Override
        public void signRequest(Token accessToken, OAuthRequest request) {
            // TODO: 20-9-16 choose one of these two or don't override this method
            request.addQuerystringParameter(OAuthConstants.ACCESS_TOKEN, accessToken.getToken());
            request.addBodyParameter(OAuthConstants.ACCESS_TOKEN, accessToken.getToken());
        }

        private void createGetRequest(Verifier verifier, OAuthRequest request) {
            request.addQuerystringParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
            request.addQuerystringParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());
            request.addQuerystringParameter(OAuthConstants.CODE, verifier.getValue());
            request.addQuerystringParameter(OAuthConstants.REDIRECT_URI, config.getCallback());
        }

        private void createPostRequest(Verifier verifier, OAuthRequest request) {
            request.addBodyParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
            request.addBodyParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());
            request.addBodyParameter(OAuthConstants.CODE, verifier.getValue());
            request.addBodyParameter(OAuthConstants.REDIRECT_URI, config.getCallback());
            request.addBodyParameter("grant_type", GRANT_TYPE);
        }
    }
}
