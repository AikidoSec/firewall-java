package dev.aikido.agent_api.background.cloud;

import com.google.gson.Gson;
import dev.aikido.agent_api.helpers.env.Token;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;

import static dev.aikido.agent_api.helpers.env.Endpoints.getAikidoRealtimeEndpoint;

public class RealtimeAPI {
    private static final Logger logger = LogManager.getLogger(RealtimeAPI.class);
    private static final int timeoutInSec = 3; // 3 sec timeout for requests to the realtime endpoint
    private final String endpoint;
    private final Token token;

    public RealtimeAPI(Token token) {
        endpoint = getAikidoRealtimeEndpoint();
        this.token = token;
    }
    public record ConfigResponse(long configUpdatedAt) {}

    private SSLContext createDefaultSSLContext() throws Exception {
        // Get the default TrustManagerFactory
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null); // Use the default trust store

        // Create an SSLContext with the default TrustManager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

        return sslContext;
    }

    public Optional<ConfigResponse> getConfig() {
        try {
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(timeoutInSec))
                    .sslContext(createDefaultSSLContext())
                    .build();
            URI uri = URI.create(endpoint + "config");
            HttpRequest request = createConfigRequest(token.get(), uri);
            // Send the request and get the response
            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return toConfigResponse(httpResponse);
        } catch (Exception e) {
            logger.debug("Error while communicating with cloud: %s", e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<ConfigResponse> toConfigResponse(HttpResponse<String> res) {
        int status = res.statusCode();
        if (status != 200) {
            logger.debug("Error occurred whilst fetching realtime config: Status code %s", status);
            return Optional.empty();
        }
        Gson gson = new Gson();
        return Optional.of(gson.fromJson(res.body(), ConfigResponse.class));
    }
    private static HttpRequest createConfigRequest(String token, URI uri) {
        return HttpRequest.newBuilder()
                .uri(uri) // Change to your target URL
                .header("Content-Type", "application/json") // Set Content-Type header
                .header("Authorization", token) // Set Authorization header
                .build();
    }
}
