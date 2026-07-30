package background.cloud;

import com.sun.net.httpserver.HttpServer;
import dev.aikido.agent_api.background.cloud.RealtimeSSEAPI;
import dev.aikido.agent_api.background.cloud.SSEParser;
import dev.aikido.agent_api.helpers.env.Token;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class RealtimeSSEAPITest {
    private HttpServer server;

    @AfterEach
    public void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    private int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private String endpointFor(int port) {
        return "http://localhost:" + port + "/";
    }

    private void writeEvent(OutputStream os, String event, String data) throws IOException {
        String payload = "event: " + event + "\ndata: " + data + "\n\n";
        os.write(payload.getBytes(StandardCharsets.UTF_8));
        os.flush();
    }

    @Test
    public void testReceivesConfigUpdatedEvent() throws Exception {
        int port = freePort();
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/runtime/stream", exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
            exchange.sendResponseHeaders(200, 0);
            writeEvent(exchange.getResponseBody(), "config-updated", "{\"configUpdatedAt\":1}");
            exchange.close();
        });
        server.start();

        RealtimeSSEAPI api = new RealtimeSSEAPI(new Token("token"), endpointFor(port), 50, 200, 1000, 5000);

        BlockingQueue<SSEParser.Event> events = new ArrayBlockingQueue<>(10);
        Thread listener = new Thread(() -> api.listen(events::add));
        listener.setDaemon(true);
        listener.start();

        SSEParser.Event event = events.poll(5, TimeUnit.SECONDS);
        listener.interrupt();
        listener.join(2000);

        assertNotNull(event);
        assertEquals("config-updated", event.event());
        assertEquals("{\"configUpdatedAt\":1}", event.data());
    }

    @Test
    public void testStopsRetryingOn401() throws Exception {
        int port = freePort();
        AtomicInteger hits = new AtomicInteger(0);
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/runtime/stream", exchange -> {
            hits.incrementAndGet();
            exchange.sendResponseHeaders(401, -1);
            exchange.close();
        });
        server.start();

        RealtimeSSEAPI api = new RealtimeSSEAPI(new Token("token"), endpointFor(port), 50, 200, 1000, 5000);

        Thread listener = new Thread(() -> api.listen(event -> {}));
        listener.setDaemon(true);
        listener.start();
        listener.join(2000);

        assertFalse(listener.isAlive());
        assertEquals(1, hits.get());
    }

    @Test
    public void testReconnectsAfter500() throws Exception {
        int port = freePort();
        AtomicInteger hits = new AtomicInteger(0);
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/runtime/stream", exchange -> {
            if (hits.getAndIncrement() == 0) {
                exchange.sendResponseHeaders(500, -1);
                exchange.close();
                return;
            }
            exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
            exchange.sendResponseHeaders(200, 0);
            writeEvent(exchange.getResponseBody(), "config-updated", "{\"configUpdatedAt\":2}");
            exchange.close();
        });
        server.start();

        RealtimeSSEAPI api = new RealtimeSSEAPI(new Token("token"), endpointFor(port), 50, 200, 1000, 5000);

        BlockingQueue<SSEParser.Event> events = new ArrayBlockingQueue<>(10);
        Thread listener = new Thread(() -> api.listen(events::add));
        listener.setDaemon(true);
        listener.start();

        SSEParser.Event event = events.poll(5, TimeUnit.SECONDS);
        listener.interrupt();
        listener.join(2000);

        assertNotNull(event);
        assertEquals(2, hits.get());
    }

    @Test
    public void testReconnectsAfterReadTimeout() throws Exception {
        int port = freePort();
        AtomicInteger hits = new AtomicInteger(0);
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/runtime/stream", exchange -> {
            if (hits.getAndIncrement() == 0) {
                exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
                exchange.sendResponseHeaders(200, 0);
                return;
            }
            exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
            exchange.sendResponseHeaders(200, 0);
            writeEvent(exchange.getResponseBody(), "config-updated", "{\"configUpdatedAt\":3}");
            exchange.close();
        });
        server.start();

        RealtimeSSEAPI api = new RealtimeSSEAPI(new Token("token"), endpointFor(port), 50, 200, 1000, 300);

        BlockingQueue<SSEParser.Event> events = new ArrayBlockingQueue<>(10);
        Thread listener = new Thread(() -> api.listen(events::add));
        listener.setDaemon(true);
        listener.start();

        SSEParser.Event event = events.poll(5, TimeUnit.SECONDS);
        listener.interrupt();
        listener.join(2000);

        assertNotNull(event);
        assertEquals(2, hits.get());
    }

    @Test
    public void testConnectionRefusedRetries() throws Exception {
        int port = freePort();
        RealtimeSSEAPI api = new RealtimeSSEAPI(new Token("token"), endpointFor(port), 50, 200, 1000, 5000);

        Thread listener = new Thread(() -> api.listen(event -> {}));
        listener.setDaemon(true);
        listener.start();
        Thread.sleep(300);
        listener.interrupt();
        listener.join(2000);

        assertFalse(listener.isAlive());
    }

    @Test
    public void testSendsExpectedRequestHeaders() throws Exception {
        int port = freePort();
        List<com.sun.net.httpserver.Headers> receivedHeaders = new CopyOnWriteArrayList<>();
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/runtime/stream", exchange -> {
            receivedHeaders.add(exchange.getRequestHeaders());
            exchange.sendResponseHeaders(401, -1);
            exchange.close();
        });
        server.start();

        RealtimeSSEAPI api = new RealtimeSSEAPI(new Token("my-token"), endpointFor(port), 50, 200, 1000, 5000);
        Thread listener = new Thread(() -> api.listen(event -> {}));
        listener.setDaemon(true);
        listener.start();
        listener.join(2000);

        assertEquals(1, receivedHeaders.size());
        com.sun.net.httpserver.Headers headers = receivedHeaders.get(0);
        assertEquals("my-token", headers.getFirst("Authorization"));
        assertEquals("text/event-stream", headers.getFirst("Accept"));
        assertEquals("no-cache", headers.getFirst("Cache-Control"));
        assertEquals("java", headers.getFirst("X-Agent-Platform"));
        assertNotNull(headers.getFirst("X-Agent-Version"));
    }

    @Test
    public void testCallbackExceptionIsTreatedAsRetryableError() throws Exception {
        int port = freePort();
        AtomicInteger hits = new AtomicInteger(0);
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/runtime/stream", exchange -> {
            hits.incrementAndGet();
            exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
            exchange.sendResponseHeaders(200, 0);
            writeEvent(exchange.getResponseBody(), "config-updated", "{\"configUpdatedAt\":1}");
            exchange.close();
        });
        server.start();

        RealtimeSSEAPI api = new RealtimeSSEAPI(new Token("token"), endpointFor(port), 20, 100, 1000, 5000);
        Thread listener = new Thread(() -> api.listen(event -> {
            throw new RuntimeException("callback exploded");
        }));
        listener.setDaemon(true);
        listener.start();

        long deadline = System.currentTimeMillis() + 2000;
        while (hits.get() < 3 && System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
        }
        listener.interrupt();
        listener.join(2000);

        assertTrue(hits.get() >= 3, "expected multiple reconnects after callback exceptions, got " + hits.get());
    }

    @Test
    public void testBackoffDoublesBetweenReconnects() throws Exception {
        int port = freePort();
        List<Long> hitTimestamps = new CopyOnWriteArrayList<>();
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/runtime/stream", exchange -> {
            hitTimestamps.add(System.currentTimeMillis());
            exchange.sendResponseHeaders(500, -1);
            exchange.close();
        });
        server.start();

        RealtimeSSEAPI api = new RealtimeSSEAPI(new Token("token"), endpointFor(port), 150, 5000, 100_000, 5000);
        Thread listener = new Thread(() -> api.listen(event -> {}));
        listener.setDaemon(true);
        listener.start();

        long deadline = System.currentTimeMillis() + 5000;
        while (hitTimestamps.size() < 4 && System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
        }
        listener.interrupt();
        listener.join(2000);

        assertTrue(hitTimestamps.size() >= 4, "expected at least 4 attempts, got " + hitTimestamps.size());
        long gap1 = hitTimestamps.get(1) - hitTimestamps.get(0);
        long gap2 = hitTimestamps.get(2) - hitTimestamps.get(1);
        long gap3 = hitTimestamps.get(3) - hitTimestamps.get(2);

        assertTrue(gap2 > gap1 * 1.2, "expected gap2 (" + gap2 + "ms) to be noticeably larger than gap1 (" + gap1 + "ms)");
        assertTrue(gap3 > gap2 * 1.2, "expected gap3 (" + gap3 + "ms) to be noticeably larger than gap2 (" + gap2 + "ms)");
    }

    @Test
    public void testBackoffResetsAfterStableConnection() throws Exception {
        int port = freePort();
        List<Long> hitTimestamps = new CopyOnWriteArrayList<>();
        AtomicInteger hits = new AtomicInteger(0);
        int holdMs = 50;
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/runtime/stream", exchange -> {
            hitTimestamps.add(System.currentTimeMillis());
            int hitNumber = hits.getAndIncrement();
            if (hitNumber < 3) {
                exchange.sendResponseHeaders(500, -1);
                exchange.close();
                return;
            }
            if (hitNumber == 3) {
                exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
                exchange.sendResponseHeaders(200, 0);
                try {
                    Thread.sleep(holdMs);
                } catch (InterruptedException ignored) {
                }
                exchange.close();
                return;
            }
            exchange.sendResponseHeaders(500, -1);
            exchange.close();
        });
        server.start();

        RealtimeSSEAPI api = new RealtimeSSEAPI(new Token("token"), endpointFor(port), 80, 5000, 30, 5000);
        Thread listener = new Thread(() -> api.listen(event -> {}));
        listener.setDaemon(true);
        listener.start();

        long deadline = System.currentTimeMillis() + 5000;
        while (hitTimestamps.size() < 5 && System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
        }
        listener.interrupt();
        listener.join(2000);

        assertTrue(hitTimestamps.size() >= 5, "expected at least 5 attempts, got " + hitTimestamps.size());
        long gapBeforeReset = hitTimestamps.get(3) - hitTimestamps.get(2);
        long gapAfterReset = hitTimestamps.get(4) - hitTimestamps.get(3);

        assertTrue(gapAfterReset < gapBeforeReset,
                "expected backoff to reset after a stable connection instead of keep growing: gapBeforeReset="
                        + gapBeforeReset + "ms gapAfterReset=" + gapAfterReset + "ms");
    }
}
