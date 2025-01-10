package dev.aikido.agent_api.background.cloud;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Optional;

import static dev.aikido.agent_api.helpers.env.Endpoints.getAikidoRealtimeEndpoint;

public class RealtimeAPI {
    private static final Logger logger = LogManager.getLogger(RealtimeAPI.class);
    private static final int timeoutInSec = 3; // 3 sec timeout for requests to the realtime endpoint
    private final String endpoint;

    public RealtimeAPI() {
        // Create API :
        endpoint = getAikidoRealtimeEndpoint();
    }

    public record ConfigResponse(long configUpdatedAt) {}

    public Optional<ConfigResponse> getConfig(String token) {
        HttpURLConnection connection = null;
        try {
            URI uri = URI.create(endpoint + "config");
            connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(timeoutInSec * 1000); // Convert seconds to milliseconds
            connection.setReadTimeout(timeoutInSec * 1000);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", token);

            // Send the request and get the response
            int responseCode = connection.getResponseCode();
            return toConfigResponse(connection, responseCode);
        } catch (Exception e) {
            logger.debug("Error while communicating with cloud: {}", e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return Optional.empty();
    }

    public Optional<ConfigResponse> toConfigResponse(HttpURLConnection connection, int status) {
        if (status != HttpURLConnection.HTTP_OK) {
            try {
                logger.debug("Error occurred whilst fetching realtime config: {}", new BufferedReader(new InputStreamReader(connection.getErrorStream())).lines().reduce("", String::concat));
            } catch (Exception e) {
                logger.debug("Error reading error stream: {}", e.getMessage());
            }
            return Optional.empty();
        }
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            Gson gson = new Gson();
            return Optional.of(gson.fromJson(response.toString(), ConfigResponse.class));
        } catch (Exception e) {
            logger.debug("Error while reading response: {}", e.getMessage());
        }
        return Optional.empty();
    }
}
