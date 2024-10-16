package dev.aikido.AikidoAgent.background.cloud.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import dev.aikido.AikidoAgent.background.cloud.api.events.APIEvent;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class ReportingApiHTTP extends ReportingApi {
    private final String reportingUrl;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ReportingApiHTTP(String reportingUrl) {
        // Reporting URL should end with trailing slash for now.
        this.reportingUrl = reportingUrl;
    }

    @Override
    public void report(String token, APIEvent event, int timeoutInSec) {
        try {
            HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutInSec))
                .build();
            URI uri = URI.create(reportingUrl + "api/runtime/events");
            HttpRequest request = createHttpRequest(event, token, uri);
            // Send the request and get the response
            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            APIResponse apiResponse = toApiResponse(httpResponse);

            System.out.println(apiResponse.configUpdatedAt());
            System.out.println(apiResponse.error());
        } catch (Exception e) {
            System.out.print("Error while communicating with cloud: " + e.getMessage() + "\n");
        }
    }
    @Override
    public APIResponse toApiResponse(HttpResponse<String> res) {
        int status = res.statusCode();
        if (status == 429) {
            return new APIResponse(false, "rate_limited", 0, null, null, null, false);
        } else if (status == 401) {
            return new APIResponse(false, "invalid_token", 0, null, null, null, false);
        } else if (status == 200) {
            Gson gson = new Gson();
            return gson.fromJson(res.body(), APIResponse.class);
        }
        return new APIResponse(false, "unknown_error", 0, null, null, null, false);

    }
    private static HttpRequest createHttpRequest(APIEvent event, String token, URI uri) {
        Gson gson = new Gson();
        String requestPayload = gson.toJson(event);
        return HttpRequest.newBuilder()
            .uri(uri) // Change to your target URL
            .header("Content-Type", "application/json") // Set Content-Type header
            .header("Authorization", token) // Set Authorization header
            .POST(HttpRequest.BodyPublishers.ofString(requestPayload)) // Set the request body
            .build();
    }
}
