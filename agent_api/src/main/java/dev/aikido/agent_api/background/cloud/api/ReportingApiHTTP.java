package dev.aikido.agent_api.background.cloud.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.storage.routes.RouteEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

public class ReportingApiHTTP extends ReportingApi {
    private static final Logger logger = LogManager.getLogger(ReportingApiHTTP.class);
    private final String reportingUrl;

    public ReportingApiHTTP(String reportingUrl) {
        // Reporting URL should end with trailing slash for now.
        this.reportingUrl = reportingUrl;
    }
    public Optional<APIResponse> fetchNewConfig(String token, int timeoutInSec) {
        try {
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(timeoutInSec))
                    .build();
            URI uri = URI.create(reportingUrl + "api/runtime/config");
            HttpRequest request = createHttpRequest(Optional.empty(), token, uri);
            // Send the request and get the response
            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return Optional.of(toApiResponse(httpResponse));
        } catch (Exception e) {
            logger.debug("Error while fetching new config from cloud: {}", e.getMessage());
        }
        return Optional.empty();
    }
    @Override
    public Optional<APIResponse> report(String token, APIEvent event, int timeoutInSec) {
        try {
            HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutInSec))
                .build();
            URI uri = URI.create(reportingUrl + "api/runtime/events");
            HttpRequest request = createHttpRequest(Optional.of(event), token, uri);
            // Send the request and get the response
            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return Optional.of(toApiResponse(httpResponse));
        } catch (Exception e) {
            logger.debug("Error while communicating with cloud: {}", e.getMessage());
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
    private static HttpRequest createHttpRequest(Optional<APIEvent> event, String token, URI uri) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(uri) // Change to your target URL
            .header("Content-Type", "application/json") // Set Content-Type header
            .header("Authorization", token); // Set Authorization header
        if (event.isPresent()) {
            Gson gson = new GsonBuilder()
                    // Use a custom serializer because api spec is transient in RouteEntry :
                    .registerTypeAdapter(RouteEntry.class, new RouteEntry.RouteEntrySerializer())
                    .create();
            String requestPayload = gson.toJson(event.get());
            return requestBuilder.POST(HttpRequest.BodyPublishers.ofString(requestPayload)) // Set the request body
                .build();
        }
        return requestBuilder.build();
    }
    private static APIResponse getUnsuccessfulAPIResponse(String error) {
        return new APIResponse(
                false, // Success
                error,
                0, null, null, null, false, false // Unimportant values.
        );
    }
}
