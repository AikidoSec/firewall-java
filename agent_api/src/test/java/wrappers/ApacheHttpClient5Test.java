package wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.storage.AttackQueue;
import dev.aikido.agent_api.storage.Hostnames;
import dev.aikido.agent_api.storage.HostnamesStore;
import dev.aikido.agent_api.storage.PendingHostnamesStore;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.io.CloseMode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.EmptySampleContextObject;

class ApacheHttpClient5Test {
    private static final AtomicInteger sourceRequests = new AtomicInteger();
    private static final AtomicInteger targetRequests = new AtomicInteger();
    private static HttpServer sourceServer;
    private static HttpServer targetServer;

    private CloseableHttpClient classicClient;
    private CloseableHttpAsyncClient asyncClient;

    @BeforeAll
    static void startServers() throws IOException {
        targetServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        targetServer.createContext("/final", exchange -> {
            targetRequests.incrementAndGet();
            respond(exchange, 204);
        });
        targetServer.start();

        sourceServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        sourceServer.createContext("/absolute", exchange -> {
            sourceRequests.incrementAndGet();
            respond(exchange, 200);
        });
        sourceServer.createContext("/relative", exchange -> {
            sourceRequests.incrementAndGet();
            respond(exchange, 200);
        });
        sourceServer.createContext("/redirect", exchange -> {
            sourceRequests.incrementAndGet();
            exchange.getResponseHeaders().add("Location", url(targetServer, "/final"));
            respond(exchange, 302);
        });
        sourceServer.start();
    }

    @AfterAll
    static void stopServers() {
        sourceServer.stop(0);
        targetServer.stop(0);
    }

    @BeforeEach
    void setUp() {
        clearState();
        ServiceConfigStore.updateBlocking(true);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (classicClient != null) {
            classicClient.close();
        }
        if (asyncClient != null) {
            asyncClient.close(CloseMode.GRACEFUL);
        }
        clearState();
    }

    @Test
    void recordsClassicAbsoluteRequestAndPreservesResponseHandler() throws IOException {
        classicClient = HttpClients.createDefault();
        int status = classicClient.execute(new HttpGet(url(sourceServer, "/absolute")), response -> response.getCode());

        assertEquals(200, status);
        assertEquals(1, sourceRequests.get());
        assertEquals(1, getHits("localhost", sourceServer.getAddress().getPort()));
    }

    @Test
    void blocksClassicHostAndRelativeTargetBeforeTheRequestIsSent() {
        classicClient = HttpClients.createDefault();
        String absoluteUrl = url(sourceServer, "/relative");
        Context.set(new EmptySampleContextObject(absoluteUrl));
        HttpHost host =
                new HttpHost("http", "localhost", sourceServer.getAddress().getPort());
        BasicClassicHttpRequest request = new BasicClassicHttpRequest("GET", "/relative");

        RuntimeException exception = assertThrows(
                RuntimeException.class, () -> classicClient.execute(host, request, response -> response.getCode()));

        assertEquals("Aikido Zen has blocked a server-side request forgery", exception.getMessage());
        assertEquals(0, sourceRequests.get());
        assertEquals(1, getHits("localhost", sourceServer.getAddress().getPort()));
        assertEquals(1, AttackQueue.getSize());
    }

    @Test
    void recordsClassicAutomaticRedirectDestination() throws IOException {
        classicClient = HttpClients.createDefault();
        EmptySampleContextObject context = new EmptySampleContextObject();
        Context.set(context);

        int status = classicClient.execute(new HttpGet(url(sourceServer, "/redirect")), response -> response.getCode());

        assertEquals(204, status);
        assertEquals(1, sourceRequests.get());
        assertEquals(1, targetRequests.get());
        assertEquals(1, getHits("localhost", sourceServer.getAddress().getPort()));
        assertEquals(1, getHits("localhost", targetServer.getAddress().getPort()));
        assertEquals(
                url(sourceServer, "/redirect"),
                context.getRedirectStartNodes().get(0).getUrl().toString());
        assertEquals(
                url(targetServer, "/final"),
                context.getRedirectStartNodes().get(0).getChild().getUrl().toString());
    }

    @Test
    void recordsAsyncRequestAndPreservesFutureAndCallback() throws Exception {
        asyncClient = HttpAsyncClients.createDefault();
        asyncClient.start();
        CountDownLatch callbackCompleted = new CountDownLatch(1);
        AtomicReference<SimpleHttpResponse> callbackResponse = new AtomicReference<>();
        AtomicReference<Exception> callbackFailure = new AtomicReference<>();
        SimpleHttpRequest request = new SimpleHttpRequest("GET", URI.create(url(sourceServer, "/absolute")));

        Future<SimpleHttpResponse> future = asyncClient.execute(request, new FutureCallback<>() {
            @Override
            public void completed(SimpleHttpResponse result) {
                callbackResponse.set(result);
                callbackCompleted.countDown();
            }

            @Override
            public void failed(Exception exception) {
                callbackFailure.set(exception);
                callbackCompleted.countDown();
            }

            @Override
            public void cancelled() {
                callbackCompleted.countDown();
            }
        });

        SimpleHttpResponse response = future.get(5, TimeUnit.SECONDS);
        assertTrue(callbackCompleted.await(5, TimeUnit.SECONDS));
        assertNull(callbackFailure.get());
        assertSame(response, callbackResponse.get());
        assertEquals(200, response.getCode());
        assertEquals(1, sourceRequests.get());
        assertEquals(1, getHits("localhost", sourceServer.getAddress().getPort()));
    }

    private static void respond(HttpExchange exchange, int status) throws IOException {
        exchange.sendResponseHeaders(status, -1);
        exchange.close();
    }

    private static String url(HttpServer server, String path) {
        return "http://localhost:" + server.getAddress().getPort() + path;
    }

    private static void clearState() {
        Context.set(null);
        HostnamesStore.clear();
        PendingHostnamesStore.clear();
        AttackQueue.clear();
        sourceRequests.set(0);
        targetRequests.set(0);
    }

    private static int getHits(String hostname, int port) {
        for (Hostnames.HostnameEntry entry : Objects.requireNonNull(HostnamesStore.getHostnamesAsList())) {
            if (entry.getHostname().equals(hostname) && entry.getPort() == port) {
                return entry.getHits();
            }
        }
        return 0;
    }
}
