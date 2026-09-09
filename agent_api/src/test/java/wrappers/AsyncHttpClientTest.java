package wrappers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.storage.Hostnames;
import dev.aikido.agent_api.storage.HostnamesStore;
import dev.aikido.agent_api.storage.PendingHostnamesStore;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import dev.aikido.agent_api.vulnerabilities.ssrf.SSRFException;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.asynchttpclient.filter.FilterContext;
import org.asynchttpclient.filter.RequestFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.EmptyAPIResponses;
import utils.EmptySampleContextObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AsyncHttpClientTest {
    private final List<HttpServer> servers = new ArrayList<>();

    @BeforeEach
    void beforeEach() {
        Context.reset();
        HostnamesStore.clear();
        PendingHostnamesStore.clear();
        ServiceConfigStore.updateFromAPIResponse(EmptyAPIResponses.emptyAPIResponse);
        ServiceConfigStore.updateBlocking(true);
    }

    @AfterEach
    void afterEach() {
        Context.reset();
        HostnamesStore.clear();
        PendingHostnamesStore.clear();
        for (HttpServer server : servers) {
            server.stop(0);
        }
    }

    @Test
    void reportsEffectiveOutboundHostnameAndPreservesAsyncHandlerResult() throws Exception {
        HttpServer server = startServer(exchange -> respond(exchange, 200, "handled"));
        String effectiveUrl = url(server, "/request");
        RequestFilter requestFilter = new RequestFilter() {
            @Override
            public <T> FilterContext<T> filter(FilterContext<T> context) {
                Request request = context.getRequest().toBuilder().setUrl(effectiveUrl).build();
                return new FilterContext.FilterContextBuilder<>(context).request(request).build();
            }
        };

        try (AsyncHttpClient client = Dsl.asyncHttpClient(Dsl.config().addRequestFilter(requestFilter))) {
            Request request = new RequestBuilder("GET").setUrl("http://unused.invalid/request").build();
            ListenableFuture<String> future = client.executeRequest(request, new AsyncCompletionHandler<>() {
                @Override
                public String onCompleted(Response response) {
                    return response.getResponseBody();
                }
            });

            assertEquals("handled", future.get(5, TimeUnit.SECONDS));
        }

        assertEquals(1, getHits("localhost", server.getAddress().getPort()));
        assertEquals(0, getHits("unused.invalid", 80));
    }

    @Test
    void blocksSsrfBeforeTheRequestReachesTheServer() throws Exception {
        AtomicInteger requestCount = new AtomicInteger();
        HttpServer server = startServer(exchange -> {
            requestCount.incrementAndGet();
            respond(exchange, 200, "unexpected");
        });
        String url = url(server, "/private");
        Context.set(new EmptySampleContextObject(url));

        try (AsyncHttpClient client = Dsl.asyncHttpClient()) {
            ExecutionException exception = assertThrows(
                    ExecutionException.class,
                    () -> client.executeRequest(new RequestBuilder("GET").setUrl(url).build())
                            .get(5, TimeUnit.SECONDS)
            );

            assertInstanceOf(SSRFException.class, rootCause(exception));
        }

        assertEquals(0, requestCount.get());
        assertEquals(1, getHits("localhost", server.getAddress().getPort()));
    }

    @Test
    void reportsEachAutomaticallyRedirectedRequestOnce() throws Exception {
        HttpServer destination = startServer(exchange -> respond(exchange, 200, "redirected"));
        HttpServer origin = startServer(exchange -> {
            exchange.getResponseHeaders().add("Location", url(destination, "/destination"));
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });

        try (AsyncHttpClient client = Dsl.asyncHttpClient(Dsl.config().setFollowRedirect(true))) {
            Response response = client.executeRequest(
                    new RequestBuilder("GET").setUrl(url(origin, "/origin")).build()
            ).get(5, TimeUnit.SECONDS);

            assertEquals(200, response.getStatusCode());
            assertEquals("redirected", response.getResponseBody());
        }

        assertEquals(1, getHits("localhost", origin.getAddress().getPort()));
        assertEquals(1, getHits("localhost", destination.getAddress().getPort()));
    }

    @Test
    void supportsLegacyNingClientRequests() throws Exception {
        HttpServer server = startServer(exchange -> respond(exchange, 200, "legacy"));
        com.ning.http.client.AsyncHttpClient client = new com.ning.http.client.AsyncHttpClient();
        try {
            com.ning.http.client.Response response = client.prepareGet(url(server, "/legacy"))
                    .execute()
                    .get(5, TimeUnit.SECONDS);
            assertEquals(200, response.getStatusCode());
            assertEquals("legacy", response.getResponseBody());
        } finally {
            client.close();
        }

        assertEquals(1, getHits("localhost", server.getAddress().getPort()));
    }

    private HttpServer startServer(com.sun.net.httpserver.HttpHandler handler) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", handler);
        server.start();
        servers.add(server);
        return server;
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private static String url(HttpServer server, String path) {
        return "http://localhost:" + server.getAddress().getPort() + path;
    }

    private static int getHits(String hostname, int port) {
        Hostnames.HostnameEntry[] entries = HostnamesStore.getHostnamesAsList();
        if (entries == null) {
            return 0;
        }
        for (Hostnames.HostnameEntry entry : entries) {
            if (entry.getHostname().equals(hostname) && entry.getPort() == port) {
                return entry.getHits();
            }
        }
        return 0;
    }

    private static Throwable rootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }
}
