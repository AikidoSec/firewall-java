package wrappers;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.storage.Hostnames;
import dev.aikido.agent_api.storage.HostnamesStore;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import okhttp3.Call;
import okhttp3.Callback;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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

    @Test
    public void testSSRFAsyncEnqueueOnDifferentThread() throws Exception {
        // newCall() runs on this test thread (intent registered here), but enqueue() executes
        // the actual call - including the DNS lookup / connect - on OkHttp's own Dispatcher
        // thread pool, a different thread. Investigating whether PendingHostnamesStore (and
        // captured Context) survive that hop the same way they need to for WebClient.
        setContextAndLifecycle("http://localhost:5000");
        assertEquals(0, getHits("localhost", 5000));

        Request request = new Request.Builder().url("http://localhost:5000").build();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicReference<Integer> responseCode = new AtomicReference<>();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                failure.set(e);
                latch.countDown();
            }

            @Override
            public void onResponse(Call call, Response response) {
                responseCode.set(response.code());
                response.close();
                latch.countDown();
            }
        });

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertTrue(completed, "enqueue() callback never fired within 5s");

        if (failure.get() == null) {
            fail("expected the SSRF block to surface as a failure, got response code: " + responseCode.get());
        }
        // OkHttp wraps the cancellation as a new IOException embedding the original exception's
        // toString() as text, not as a proper getCause() chain, so check the message contains it.
        String message = failure.get().getMessage();
        assertTrue(message != null && message.contains("Aikido Zen has blocked a server-side request forgery"),
                "expected an SSRF block in the failure message, got: " + failure.get());
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
