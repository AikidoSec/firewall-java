package dev.aikido.agent_api.background.cloud.api;

import com.google.gson.Gson;
import dev.aikido.agent_api.background.cloud.AgentHeaders;
import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.helpers.env.Token;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

import static dev.aikido.agent_api.background.cloud.SSLContextHelper.createDefaultSSLContext;

public class ReportingApiHTTP extends ReportingApi {
    private final Logger logger = LogManager.getLogger(ReportingApiHTTP.class);
    private final String reportingUrl;
    private final Token token;
    private final Gson gson = new Gson();
    private HttpClient httpClient;
    public ReportingApiHTTP(String reportingUrl, int timeoutInSec, Token token) {
        // Reporting URL should end with trailing slash for now.
        super(timeoutInSec);
        this.reportingUrl = reportingUrl;
        this.token = token;
    }

    private synchronized HttpClient getHttpClient() throws Exception {
        if (httpClient == null) {
            httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(timeoutInSec))
                    .sslContext(createDefaultSSLContext())
                    .build();
        }
        return httpClient;
    }

    public Optional<APIResponse> fetchNewConfig() {
        try {
            URI uri = URI.create(reportingUrl + "api/runtime/config");
            HttpRequest request = createHttpRequest(Optional.empty(), uri);

            // Send the request and get the response
            HttpResponse<String> httpResponse = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            logger.trace("Got response for %s: %s", uri.toString(), httpResponse.body());
            return Optional.of(toApiResponse(httpResponse));
        } catch (Exception e) {
            logger.debug("Error while fetching new config from cloud: %s", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<APIResponse> report(APIEvent event) {
        try {
            URI uri = URI.create(reportingUrl + "api/runtime/events");
            HttpRequest request = createHttpRequest(Optional.of(event), uri);

            // Send the request and get the response
            HttpResponse<String> httpResponse = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            logger.trace("Got response for %s: %s", uri.toString(), httpResponse.body());
            return Optional.of(toApiResponse(httpResponse));
        } catch (Exception e) {
            logger.debug("Error while communicating with cloud: %s", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<APIListsResponse> fetchBlockedLists() {
        if (token == null) {
            return Optional.empty();
        }
        try {
            URI uri = URI.create(reportingUrl + "api/runtime/firewall/lists");
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(timeoutInSec))
                    .header("Accept-Encoding", "gzip")
                    .header("Authorization", token.get());
            AgentHeaders.get().forEach(requestBuilder::header);
            HttpRequest request = requestBuilder.build();

            // Send the request and get the response
            HttpResponse<InputStream> httpResponse = getHttpClient().send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream body = httpResponse.body()) {
                if (httpResponse.statusCode() != HttpURLConnection.HTTP_OK) {
                    return Optional.empty();
                }

                boolean gzipped = "gzip".equalsIgnoreCase(httpResponse.headers().firstValue("Content-Encoding").orElse(""));
                try (InputStream inputStream = gzipped ? new GZIPInputStream(body) : body) {
                    return Optional.of(gson.fromJson(new InputStreamReader(inputStream), APIListsResponse.class));
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to fetch blocked lists: %s", e.getMessage());
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
            try {
                return new Gson().fromJson(res.body(), APIResponse.class);
            } catch (Throwable e) {
                logger.debug("json error: %s", e);
                return getUnsuccessfulAPIResponse("json_deserialize");
            }
        }
        return getUnsuccessfulAPIResponse("unknown_error");
    }
    private HttpRequest createHttpRequest(Optional<APIEvent> event, URI uri) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(uri) // Change to your target URL
            .timeout(Duration.ofSeconds(timeoutInSec))
            .header("Content-Type", "application/json") // Set Content-Type header
            .header("Authorization", token.get()); // Set Authorization header
        AgentHeaders.get().forEach(requestBuilder::header);
        if (event.isPresent()) {
            Gson gson = new Gson();
            String requestPayload = gson.toJson(event.get());
            logger.trace("New request payload: %s", requestPayload);
            return requestBuilder.POST(HttpRequest.BodyPublishers.ofString(requestPayload)) // Set the request body
                .build();
        }
        return requestBuilder.build();
    }
    private static APIResponse getUnsuccessfulAPIResponse(String error) {
        return new APIResponse(
                false, // Success
                error,
                0, null, null, null, false, null, false, false, null, 0 // Unimportant values.
        );
    }
}
