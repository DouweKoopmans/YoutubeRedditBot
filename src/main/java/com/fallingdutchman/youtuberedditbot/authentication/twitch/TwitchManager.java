package com.fallingdutchman.youtuberedditbot.authentication.twitch;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Triple;

import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Singleton
@Slf4j
public class TwitchManager {
    private static final String API_VERSION_NAME = "helix";

    @Synchronized
    public Optional<JsonObject> genericTwitchRequest(final String endPoint, final String clientId)
            throws MalformedURLException{
        try {
            log.trace("making request to twitch at \"" + endPoint + "\" with client_id \"" + clientId
                + "\" api version: \"" + API_VERSION_NAME);
            JsonObject baseElement;
            val jsonParser = new JsonParser();
            val url = String.format("https://api.twitch.tv/%s/%s", endPoint, API_VERSION_NAME);
            val connection = (HttpURLConnection) (new URL(url).openConnection());
            connection.setRequestProperty("Client-ID", clientId);
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);

            final int responseCode = connection.getResponseCode();
            log.trace("response code: " + responseCode);

            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                if (responseCode != 200) {
                    final String body = bufferedReader.lines().collect(Collectors.joining("\n"));
                    errorHandler(responseCode, connection.getResponseMessage(), body);
                    return Optional.empty();
                } else {
                    baseElement = jsonParser.parse(new InputStreamReader(connection.getInputStream())).getAsJsonObject();
                    log.trace("json response: " + baseElement.toString());
                }
                return Optional.of(baseElement);
            }
        }  catch (IOException e) {
            log.error("an IO exception occurred trying to make a request to twitch", e);
            return Optional.empty();
        }
    }

    private void errorHandler(int responseCode, String responseMessage, String body) {
        errorConsumer().accept(Triple.of(responseCode, responseMessage, body));
    }

    private Consumer<Triple<Integer, String, String>> errorConsumer() {
        return errorTriple -> {
            if (errorTriple.getLeft() > 499 && errorTriple.getLeft() <=599) {
                log.warn("twitch has an internal error");
                log.warn("error code: " + errorTriple.getLeft());
                log.warn("error message: " + errorTriple.getRight());
            } else if (errorTriple.getLeft() > 399 && errorTriple.getLeft() <= 499) {
                log.error("twitch returned a 400 error, please check the request");
                log.error("error code: " + errorTriple.getLeft());
                log.error("error message: " + errorTriple.getMiddle());
                log.error("error body: " + errorTriple.getRight());
            }
        };
    }
}
