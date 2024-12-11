package wrappers;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.storage.routes.Routes;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;
import dev.aikido.agent_api.vulnerabilities.ssrf.SSRFException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InetAddressTest {
    private HttpClient httpClient;
    public static class SampleContextObject extends ContextObject {
        public SampleContextObject(String argument) {
            this.method = "GET";
            this.source = "web";
            this.url = "https://example.com/api/resource";
            this.route = "/api/resource";
            this.remoteAddress = "192.168.1.1";
            this.headers = new HashMap<>();

            this.query = new HashMap<>();
            this.query.put("search", new String[]{"example", "dev.aikido:80"});
            this.query.put("sql1", new String[]{"SELECT * FRO"});
            this.query.put("arg", new String[]{argument});

            this.cookies = new HashMap<>();
            this.body = "{\"key\":\"value\"}"; // Body as a JSON string
        }
    }

    @AfterEach
    void cleanup() {
        Context.set(null);
        ThreadCache.set(null);
    }
    @BeforeEach
    void clearThreadCache() {
        httpClient = HttpClient.newHttpClient();
        cleanup();
        ThreadCache.set(new ThreadCacheObject(List.of(), Set.of(), Set.of(), new Routes()));
    }
    private void setContextAndLifecycle(String url) {
        Context.set(new SampleContextObject(url));
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    public void testSSRFLocalhostValid() throws Exception {
        setContextAndLifecycle("http://localhost:5000");

        assertThrows(RuntimeException.class, () -> {
            fetchResponse("http://localhost:5000/api/test");
        });

        assertThrows(RuntimeException.class, () -> {
            fetchResponse("http://localhost:5000");
        });

        assertThrows(RuntimeException.class, () -> {
            fetchResponse("https://localhost:5000/api/test");
        });
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    public void testSSRFWithoutPort() throws Exception {
        setContextAndLifecycle("http://localhost:80");
        assertThrows(RuntimeException.class, () -> {
            fetchResponse("http://localhost/api/test");
        });
    }

    private void fetchResponse(String urlString) throws IOException, SSRFException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer invalid-token-2");
        connection.setConnectTimeout(5000); // Set connection timeout
        connection.setReadTimeout(5000); // Set read timeout

        int responseCode = connection.getResponseCode();
    }
}