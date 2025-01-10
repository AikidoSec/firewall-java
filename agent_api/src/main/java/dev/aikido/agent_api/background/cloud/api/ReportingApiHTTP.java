package dev.aikido.agent_api.background.cloud.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.storage.routes.RouteEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

public class ReportingApiHTTP extends ReportingApi {
    private static final Logger logger = LogManager.getLogger(ReportingApiHTTP.class);
    private final String reportingUrl;
    private final Gson gson = new Gson();
    public ReportingApiHTTP(String reportingUrl) {
        // Reporting URL should end with trailing slash for now.
        this.reportingUrl = reportingUrl;
    }

    public Optional<APIResponse> fetchNewConfig(String token, int timeoutInSec) {
        HttpURLConnection connection = null;
        try {
            URI uri = URI.create(reportingUrl + "api/runtime/config");
            connection = createHttpRequest(Optional.empty(), token, uri);
            connection.setConnectTimeout(timeoutInSec * 1000);
            connection.setReadTimeout(timeoutInSec * 1000);

            // Send the request and get the response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Handle response...
            } else {
                logger.debug("Error while fetching new config from cloud: HTTP response code {}", responseCode);
            }
        } catch (Exception e) {
            logger.debug("Error while fetching new config from cloud: {}", e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<APIResponse> report(String token, APIEvent event, int timeoutInSec) {
        HttpURLConnection connection = null;
        try {
            URI uri = URI.create(reportingUrl + "api/runtime/events");
            connection = createHttpRequest(Optional.of(event), token, uri);
            connection.setConnectTimeout(timeoutInSec * 1000);
            connection.setReadTimeout(timeoutInSec * 1000);

            // Send the request and get the response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Handle response...
            } else {
                logger.debug("Error while communicating with cloud: HTTP response code {}", responseCode);
            }
        } catch (Exception e) {
            logger.debug("Error while communicating with cloud: {}", e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<APIListsResponse> fetchBlockedLists(String token) {
        if (token == null) {
            return Optional.empty();
        }
        try {
            // Make a GET request to api/runtime/firewall/lists
            URL url = new URL(reportingUrl + "api/runtime/firewall/lists");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Set the Accept-Encoding header to gzip
            connection.setRequestProperty("Accept-Encoding", "gzip");
            connection.setRequestProperty("Authorization", token);

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return Optional.empty();
            }
            InputStream inputStream = connection.getInputStream();
            // Check if the response is gzipped
            if ("gzip".equalsIgnoreCase(connection.getContentEncoding())) {
                inputStream = new GZIPInputStream(inputStream);
            }

            // Read the response :
            APIListsResponse res = gson.fromJson(new InputStreamReader(inputStream), APIListsResponse.class);
            return Optional.of(res);
        } catch (Exception e) {
            logger.debug("Failed to fetch blocked lists: {}", e);
        }
        return Optional.empty();
    }

    @Override
    public APIResponse toApiResponse(HttpResponse<String> res) {
        int status = res.statusCode();
        if (status == 429) {
            return getUnsuccessfulAPIResponse("rate_limited");
        } else if (status == 401) {
            return getUnsuccessfulAPIResponse("invalid_token");
        } else if (status == 200) {
            Gson gson = new Gson();
            return gson.fromJson(res.body(), APIResponse.class);
        }
        return getUnsuccessfulAPIResponse("unknown_error");
    }
    private static HttpURLConnection createHttpRequest(Optional<APIEvent> event, String token, URI uri) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod(event.isPresent() ? "POST" : "GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", token);
        connection.setDoOutput(event.isPresent()); // Enable output if there's a request body

        if (event.isPresent()) {
            Gson gson = new GsonBuilder()
                    // Use a custom serializer because api spec is transient in RouteEntry:
                    .registerTypeAdapter(RouteEntry.class, new RouteEntry.RouteEntrySerializer())
                    .create();
            String requestPayload = gson.toJson(event.get());

            // Write the request body
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
        }

        return connection;
    }

    private static APIResponse getUnsuccessfulAPIResponse(String error) {
        return new APIResponse(
                false, // Success
                error,
                0, null, null, null, false, false // Unimportant values.
        );
    }
}
