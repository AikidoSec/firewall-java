package dev.aikido.AikidoAgent.background.cloud.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

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
    public void report(String token, String event, int timeoutInSec) {
        try {
            HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutInSec))
                .build();
            URI uri = URI.create(reportingUrl + "api/runtime/events");
            HttpRequest request = createHttpRequest(event, token, uri);

            // Send the request and get the response
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Print the response status and body
            System.out.println(response.statusCode());
            System.out.println(response.body());

        } catch (Exception e) {
            System.out.print("Error: " + e.getMessage() + "\n");
            System.out.print("Error type: " + e.getClass().getSimpleName() + "\n");
            // Handle specific exceptions if needed
        }
    }
    private static HttpRequest createHttpRequest(Object event, String token, URI uri) {
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
