package wrappers;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import dev.aikido.agent_api.vulnerabilities.ssrf.SSRFException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.EmptySampleContextObject;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class InetAddressTest {
    @AfterEach
    void cleanup() {
        Context.set(null);
    }
    @BeforeEach
    void beforeEach() {
        cleanup();
        ServiceConfigStore.updateBlocking(true);
    }
    private void setContextAndLifecycle(String url) {
        Context.set(new EmptySampleContextObject(url));
    }

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

    @Test
    public void testSSRFWithoutPort() throws Exception {
        setContextAndLifecycle("http://localhost:80");
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fetchResponse("http://localhost/api/test");
        });
        assertEquals("dev.aikido.agent_api.vulnerabilities.ssrf.SSRFException: Aikido Zen has blocked a server-side request forgery", exception.getMessage());
    }

    @Test
    public void testSSRFWithoutPortAndWithoutContext() throws Exception {
        setContextAndLifecycle("http://localhost:80");
        Context.set(null);
        assertThrows(ConnectException.class, () -> {
            fetchResponse("http://localhost/api/test");
        });
    }

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