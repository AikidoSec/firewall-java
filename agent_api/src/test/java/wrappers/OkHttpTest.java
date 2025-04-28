package wrappers;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.storage.Hostnames;
import dev.aikido.agent_api.storage.HostnamesStore;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.EmptySampleContextObject;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OkHttpTest {
    private OkHttpClient client;

    @AfterEach
    void cleanup() {
        Context.set(null);
        HostnamesStore.clear();
    }

    @BeforeEach
    void beforeEach() {
        cleanup();
        client = new OkHttpClient();
        ServiceConfigStore.updateBlocking(true);
    }

    private void setContextAndLifecycle(String url) {
        Context.set(new EmptySampleContextObject(url));
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
                exception1.getMessage());
        assertEquals(1, getHits("localhost", 5000));


        RuntimeException exception2 = assertThrows(RuntimeException.class, () -> {
            fetchResponse("http://localhost:5000");
        });
        assertEquals(
                "Aikido Zen has blocked a server-side request forgery",
                exception2.getMessage());
        assertEquals(2, getHits("localhost", 5000));


        RuntimeException exception3 = assertThrows(RuntimeException.class, () -> {
            fetchResponse("https://localhost:5000/api/test");
        });
        assertEquals(
                "Aikido Zen has blocked a server-side request forgery",
                exception3.getMessage());
        assertEquals(3, getHits("localhost", 5000));

    }

    @Test
    public void testSSRFWithoutPort() throws Exception {
        setContextAndLifecycle("http://localhost:80");
        assertEquals(0, getHits("localhost", 80));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fetchResponse("http://localhost/api/test");
        });
        assertEquals("Aikido Zen has blocked a server-side request forgery", exception.getMessage());
        assertEquals(1, getHits("localhost", 80));

    }

    @Test
    public void testSSRFWithoutPortAndWithoutContext() throws Exception {
        assertEquals(0, getHits("localhost", 80));

        setContextAndLifecycle("http://localhost:80");
        Context.set(null);
        assertThrows(ConnectException.class, () -> {
            fetchResponse("http://localhost/api/test");
        });
        assertEquals(1, getHits("localhost", 80));
    }

    private void fetchResponse(String urlString) throws IOException {
        Request request = new Request.Builder()
                .url(urlString)
                .addHeader("Authorization", "Bearer invalid-token-2")
                .build();
        Response response = client.newCall(request).execute();
    }
    private int getHits(String hostname, int port) {
        for (Hostnames.HostnameEntry entry: Objects.requireNonNull(HostnamesStore.getHostnamesAsList())) {
            if (entry.getHostname().equals(hostname) && entry.getPort() == port) {
                return entry.getHits();
            }
        }
        return 0;
    }
}
