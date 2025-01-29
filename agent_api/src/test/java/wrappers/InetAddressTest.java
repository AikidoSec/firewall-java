package wrappers;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.vulnerabilities.ssrf.SSRFException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import utils.EmptySampleContextObject;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static utils.EmtpyThreadCacheObject.getEmptyThreadCacheObject;

public class InetAddressTest {
    private HttpClient httpClient;

    @AfterEach
    void cleanup() {
        Context.set(null);
        ThreadCache.set(null);
    }

    @BeforeEach
    void clearThreadCache() {
        httpClient = HttpClient.newHttpClient();
        cleanup();
    }

    private void setContextAndLifecycle(String url) {
        Context.set(new EmptySampleContextObject(url));
        ThreadCache.set(getEmptyThreadCacheObject());
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCK", value = "true")
    @Test
    public void testSSRFLocalhostValid() throws Exception {
        setContextAndLifecycle("http://localhost:5000");

        RuntimeException exception1 = assertThrows(RuntimeException.class, () -> {
            fetchResponse("http://localhost:5000/api/test");
        });
        assertEquals(
            "dev.aikido.agent_api.vulnerabilities.ssrf.SSRFException: Aikido Zen has blocked a server-side request forgery",
            exception1.getMessage());

        RuntimeException exception2 = assertThrows(RuntimeException.class, () -> {
            fetchResponse("http://localhost:5000");
        });
        assertEquals(
            "dev.aikido.agent_api.vulnerabilities.ssrf.SSRFException: Aikido Zen has blocked a server-side request forgery",
            exception2.getMessage());


        RuntimeException exception3 = assertThrows(RuntimeException.class, () -> {
            fetchResponse("https://localhost:5000/api/test");
        });
        assertEquals(
            "dev.aikido.agent_api.vulnerabilities.ssrf.SSRFException: Aikido Zen has blocked a server-side request forgery",
            exception3.getMessage());

    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCK", value = "true")
    @Test
    public void testSSRFWithoutPort() throws Exception {
        setContextAndLifecycle("http://localhost:80");
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fetchResponse("http://localhost/api/test");
        });
        assertEquals("dev.aikido.agent_api.vulnerabilities.ssrf.SSRFException: Aikido Zen has blocked a server-side request forgery", exception.getMessage());
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCK", value = "true")
    @Test
    public void testSSRFWithoutPortAndWithoutContext() throws Exception {
        setContextAndLifecycle("http://localhost:80");
        Context.set(null);
        assertThrows(ConnectException.class, () -> {
            fetchResponse("http://localhost/api/test");
        });
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCK", value = "true")
    @Test
    public void testSSRFWithoutPortAndWithoutThreadCache() throws Exception {
        setContextAndLifecycle("http://localhost:80");
        ThreadCache.set(null);
        assertThrows(ConnectException.class, () -> {
            fetchResponse("http://localhost/weirdroute");
        });
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCK", value = "true")
    @Test
    public void testSSRFWithHttpClient() {
        setContextAndLifecycle("http://localhost:5000/");

        Exception exception1 = assertThrows(Exception.class, () -> {
            fetchResponseHttpClient("http://localhost:5000/config");
        });
        assertTrue(exception1.getMessage().endsWith("Aikido Zen has blocked a server-side request forgery"));

        Exception exception2 = assertThrows(Exception.class, () -> {
            fetchResponseHttpClient("http://localhost:5000/mock/events");
        });
        assertTrue(exception2.getMessage().endsWith("Aikido Zen has blocked a server-side request forgery"));

        Exception exception3 = assertThrows(Exception.class, () -> {
            fetchResponseHttpClient("https://localhost:5000/api/runtime/config");
        });
        assertTrue(exception3.getMessage().endsWith("Aikido Zen has blocked a server-side request forgery"));


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

    private void fetchResponseHttpClient(String urlString) throws InterruptedException, IOException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(urlString))
            .GET() // GET is the default method, so this line is optional
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
