package wrappers;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.storage.Hostnames;
import dev.aikido.agent_api.storage.HostnamesStore;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;
import utils.EmptySampleContextObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HttpURLConnectionTest {

    @AfterEach
    void cleanup() {
        Context.set(null);
        HostnamesStore.clear();
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
    public void testNewUrlConnectionWithPort() throws Exception {
        setContextAndLifecycle("http://localhost:8080");
        assertEquals(0, getHits("localhost", 8080));

        assertThrows(RuntimeException.class, () -> {
            fetchResponse("http://localhost:8080");
        });
        assertEquals(1, getHits("localhost", 8080));
    }

    @Test
    public void testNewUrlConnectionWithHttp() throws Exception {
        setContextAndLifecycle("http://app.local.aikido.io");
        assertEquals(0, getHits("app.local.aikido.io", 80));
        assertThrows(RuntimeException.class, () -> {
            fetchResponse("http://app.local.aikido.io");
        });
        assertEquals(1, getHits("app.local.aikido.io", 80));
    }

    @Test
    public void testNewUrlConnectionWithHttpAsHttp() throws Exception {
        setContextAndLifecycle("http://app.local.aikido.io");
        assertEquals(0, getHits("app.local.aikido.io", 80));
        assertThrows(RuntimeException.class, () -> {
            fetchResponse("http://app.local.aikido.io");
        });
        assertEquals(1, getHits("app.local.aikido.io", 80));
    }

    @Test
    public void testNewUrlConnectionHttps() throws Exception {
        setContextAndLifecycle("https://aikido.dev");
        assertEquals(0, getHits("aikido.dev", 443));

        fetchResponse("https://aikido.dev");
        assertEquals(1, getHits("aikido.dev", 443));
    }

    @Test
    public void testNewUrlConnectionFaultyProtocol() throws Exception {
        setContextAndLifecycle("ftp://localhost:8080");
        assertEquals(0, getHits("localhost", 8080));

        assertThrows(ClassCastException.class, () -> {
            fetchResponse("ftp://localhost:8080");
        });
        assertEquals(0, getHits("localhost", 8080));
    }

    @Test
    public void testSSRFLocalhostValid() throws Exception {
        assertEquals(0, getHits("localhost", 5000));
        setContextAndLifecycle("http://localhost:5000");

        RuntimeException exception1 = assertThrows(RuntimeException.class, () -> {
            fetchResponse("http://localhost:5000/api/test");
        });
        assertEquals(
            "Aikido Zen has blocked a server-side request forgery",
            exception1.getCause().getMessage());
        assertEquals(1, getHits("localhost", 5000));

        RuntimeException exception2 = assertThrows(RuntimeException.class, () -> {
            fetchResponse("http://localhost:5000");
        });
        assertEquals(
            "Aikido Zen has blocked a server-side request forgery",
            exception2.getCause().getMessage());
        assertEquals(2, getHits("localhost", 5000));

        RuntimeException exception3 = assertThrows(RuntimeException.class, () -> {
            fetchResponse("https://localhost:5000/api/test");
        });
        assertEquals(
            "Aikido Zen has blocked a server-side request forgery",
            exception3.getCause().getMessage());
        assertEquals(3, getHits("localhost", 5000));
    }

    @Test
    public void testSSRFWithoutPort() throws Exception {
        setContextAndLifecycle("http://localhost:80");
        assertEquals(0, getHits("localhost", 80));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fetchResponse("http://localhost/api/test");
        });
        assertEquals("Aikido Zen has blocked a server-side request forgery", exception.getCause().getMessage());
        assertEquals(1, getHits("localhost", 80));
    }

    @Test
    public void testSSRFWithoutPortAndWithoutContext() throws Exception {
        setContextAndLifecycle("http://localhost:80");
        assertEquals(0, getHits("localhost", 80));
        Context.set(null);
        assertThrows(ResourceAccessException.class, () -> {
            fetchResponse("http://localhost/api/test");
        });
        assertEquals(1, getHits("localhost", 80));
    }

    private void fetchResponse(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(true);
        connection.getResponseCode();
    }

    private int getHits(String hostname, int port) {
        for (Hostnames.HostnameEntry entry : Objects.requireNonNull(HostnamesStore.getHostnamesAsList())) {
            if (entry.getHostname().equals(hostname) && entry.getPort() == port) {
                return entry.getHits();
            }
        }
        return 0;
    }
}
