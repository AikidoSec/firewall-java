package background.cloud;

import com.sun.net.httpserver.HttpServer;
import dev.aikido.agent_api.background.cloud.api.ReportingApiHTTP;
import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.helpers.env.Token;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(10)
public class ReportingAPIResourceTest {
    @Test
    public void testReusesConnectionsAcrossCloudRequests() throws Exception {
        Set<InetSocketAddress> clients = ConcurrentHashMap.newKeySet();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            clients.add(exchange.getRemoteAddress());
            exchange.getRequestBody().readAllBytes();
            byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (var output = exchange.getResponseBody()) {
                output.write(body);
            }
        });
        server.start();
        try {
            ReportingApiHTTP api = new ReportingApiHTTP("http://127.0.0.1:" + server.getAddress().getPort() + "/", 2, 2, new Token("token"));
            record Event(String type) implements APIEvent {}
            for (int i = 0; i < 10; i++) {
                assertTrue(api.fetchNewConfig().isPresent());
                assertTrue(api.fetchBlockedLists().isPresent());
                assertTrue(api.report(new Event("heartbeat")).isPresent());
            }
            assertEquals(1, clients.size());
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void testClosesRejectedResponseWithUnfinishedBody() throws Exception {
        assertClosesUnfinishedResponse("503 Service Unavailable", "", "no");
    }

    @Test
    public void testClosesResponseWhenJsonParsingFails() throws Exception {
        assertClosesUnfinishedResponse("200 OK", "", "[]     \n");
    }

    @Test
    public void testClosesResponseWhenGzipHeaderIsInvalid() throws Exception {
        assertClosesUnfinishedResponse("200 OK", "Content-Encoding: gzip\r\n", "no");
    }

    private void assertClosesUnfinishedResponse(String status, String headers, String body) throws Exception {
        try (ServerSocket server = new ServerSocket(0, 1, java.net.InetAddress.getLoopbackAddress())) {
            var executor = Executors.newSingleThreadExecutor();
            try {
                var disconnected = executor.submit(() -> {
                    try (var socket = server.accept()) {
                        socket.setSoTimeout(3000);
                        var reader = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
                        while (!reader.readLine().isEmpty()) {}
                        var output = socket.getOutputStream();
                        output.write(("HTTP/1.1 " + status + "\r\nContent-Length: 1048576\r\n" + headers + "\r\n" + body)
                                .getBytes(StandardCharsets.US_ASCII));
                        output.flush();
                        return socket.getInputStream().read();
                    }
                });
                ReportingApiHTTP api = new ReportingApiHTTP("http://localhost:" + server.getLocalPort() + "/", 1, 1, new Token("token"));
                assertTrue(api.fetchBlockedLists().isEmpty());
                assertEquals(-1, disconnected.get(4, TimeUnit.SECONDS));
            } finally {
                executor.shutdownNow();
            }
        }
    }

    @Test
    public void testReadsGzippedLists() throws Exception {
        var bytes = new ByteArrayOutputStream();
        try (var gzip = new GZIPOutputStream(bytes)) {
            gzip.write("{\"blockedUserAgents\":\"bot\"}".getBytes(StandardCharsets.UTF_8));
        }
        byte[] body = bytes.toByteArray();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            exchange.getResponseHeaders().add("Content-Encoding", "gzip");
            exchange.sendResponseHeaders(200, body.length);
            try (var output = exchange.getResponseBody()) {
                output.write(body);
            }
        });
        server.start();
        try {
            ReportingApiHTTP api = new ReportingApiHTTP("http://127.0.0.1:" + server.getAddress().getPort() + "/", 2, 2, new Token("token"));
            assertEquals("bot", api.fetchBlockedLists().orElseThrow().blockedUserAgents());
        } finally {
            server.stop(0);
        }
    }
}
