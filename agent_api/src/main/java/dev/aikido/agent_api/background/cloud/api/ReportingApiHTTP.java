package dev.aikido.agent_api.background.cloud.api;

import com.google.gson.Gson;
import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.helpers.env.Token;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;

public class ReportingApiHTTP extends ReportingApi {
    private final Logger logger = LogManager.getLogger(ReportingApiHTTP.class);
    private final String reportingUrl;
    private final Token token;
    private final Gson gson = new Gson();
    public ReportingApiHTTP(String reportingUrl, int timeoutInSec, Token token) {
        // Reporting URL should end with trailing slash for now.
        super(timeoutInSec);
        this.reportingUrl = reportingUrl;
        this.token = token;
    }

    private SSLContext createDefaultSSLContext() throws Exception {
        // Get the default TrustManagerFactory
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null); // Use the default trust store

        // Create an SSLContext with the default TrustManager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

        return sslContext;
    }

    public Optional<APIResponse> fetchNewConfig() {
        try {
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(timeoutInSec))
                    .sslContext(createDefaultSSLContext())
                    .build();

            URI uri = URI.create(reportingUrl + "api/runtime/config");
            HttpRequest request = createHttpRequest(Optional.empty(), uri);

            // Send the request and get the response
            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
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
            HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutInSec))
                .sslContext(createDefaultSSLContext())
                .build();

            URI uri = URI.create(reportingUrl + "api/runtime/events");
            HttpRequest request = createHttpRequest(Optional.of(event), uri);

            // Send the request and get the response
            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
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
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(timeoutInSec))
                    .sslContext(createDefaultSSLContext())
                    .build();

            URI uri = URI.create(reportingUrl + "api/runtime/firewall/lists");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(timeoutInSec))
                    .header("Accept-Encoding", "gzip")
                    .header("Authorization", token.get())
                    .build();

            // Send the request and get the response
            HttpResponse<InputStream> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (httpResponse.statusCode() != HttpURLConnection.HTTP_OK) {
                return Optional.empty();
            }

            InputStream inputStream = httpResponse.body();
            // Check if the response is gzipped
            if ("gzip".equalsIgnoreCase(httpResponse.headers().firstValue("Content-Encoding").orElse(""))) {
                inputStream = new GZIPInputStream(inputStream);
            }

            // Read the response
            APIListsResponse res = gson.fromJson(new InputStreamReader(inputStream), APIListsResponse.class);
            return Optional.of(res);
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
                0, null, null, null, false, false // Unimportant values.
        );
    }
}
