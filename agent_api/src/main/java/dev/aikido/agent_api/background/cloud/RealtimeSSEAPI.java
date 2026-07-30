package dev.aikido.agent_api.background.cloud;

import dev.aikido.agent_api.Config;
import dev.aikido.agent_api.helpers.env.Token;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Consumer;

import static dev.aikido.agent_api.helpers.env.Endpoints.getAikidoAPIEndpoint;

public class RealtimeSSEAPI {
    private static final Logger logger = LogManager.getLogger(RealtimeSSEAPI.class);
    private static final int DEFAULT_INITIAL_RECONNECT_MS = 5_000;
    private static final int DEFAULT_MAX_RECONNECT_MS = 60_000;
    private static final int DEFAULT_STABLE_CONNECTION_MS = 30_000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 70_000;
    private static final int CONNECT_TIMEOUT_MS = 10_000;

    private final Token token;
    private final String realtimeEndpoint;
    private final int initialReconnectMs;
    private final int maxReconnectMs;
    private final int stableConnectionMs;
    private final int readTimeoutMs;

    public RealtimeSSEAPI(Token token) {
        this(token, getAikidoAPIEndpoint(token), DEFAULT_INITIAL_RECONNECT_MS, DEFAULT_MAX_RECONNECT_MS, DEFAULT_STABLE_CONNECTION_MS, DEFAULT_READ_TIMEOUT_MS);
    }

    public RealtimeSSEAPI(Token token, String realtimeEndpoint, int initialReconnectMs, int maxReconnectMs, int stableConnectionMs, int readTimeoutMs) {
        this.token = token;
        this.realtimeEndpoint = realtimeEndpoint;
        this.initialReconnectMs = initialReconnectMs;
        this.maxReconnectMs = maxReconnectMs;
        this.stableConnectionMs = stableConnectionMs;
        this.readTimeoutMs = readTimeoutMs;
    }

    private enum Outcome { ERROR, DISCONNECTED, REJECTED }

    private record ConnectResult(Outcome outcome, int statusCode) {}

    public void listen(Consumer<SSEParser.Event> onEvent) {
        int reconnectMs = initialReconnectMs;
        while (!Thread.currentThread().isInterrupted()) {
            long start = System.currentTimeMillis();
            ConnectResult result = connect(onEvent);

            if (result.outcome() == Outcome.REJECTED) {
                logger.info("SSE connection rejected with status %s, stopping", result.statusCode());
                return;
            }

            if (System.currentTimeMillis() - start >= stableConnectionMs) {
                reconnectMs = initialReconnectMs;
            }

            long jitter = (long) (Math.random() * (reconnectMs / 2.0));
            long delayMs = reconnectMs + jitter;
            reconnectMs = Math.min(reconnectMs * 2, maxReconnectMs);

            logger.trace("SSE scheduling reconnect in %sms", delayMs);
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private ConnectResult connect(Consumer<SSEParser.Event> onEvent) {
        logger.trace("SSE connecting to realtime endpoint");
        HttpURLConnection connection;
        try {
            URI uri = URI.create(realtimeEndpoint + "api/runtime/stream");
            connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", token.get());
            connection.setRequestProperty("Accept", "text/event-stream");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setRequestProperty("X-Agent-Platform", "java");
            connection.setRequestProperty("X-Agent-Version", Config.pkgVersion);
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(readTimeoutMs);
        } catch (IOException e) {
            logger.debug("SSE connection error: %s", e.getMessage());
            return new ConnectResult(Outcome.ERROR, -1);
        }

        try {
            int statusCode = connection.getResponseCode();
            if (statusCode != 200) {
                if (statusCode == 401 || statusCode == 403) {
                    return new ConnectResult(Outcome.REJECTED, statusCode);
                }
                return new ConnectResult(Outcome.DISCONNECTED, statusCode);
            }

            logger.debug("SSE connected successfully");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                SSEParser parser = new SSEParser(reader);
                Optional<SSEParser.Event> event;
                while ((event = parser.nextEvent()).isPresent()) {
                    onEvent.accept(event.get());
                }
            }
            logger.debug("SSE connection closed by server");
            return new ConnectResult(Outcome.DISCONNECTED, statusCode);
        } catch (IOException e) {
            logger.debug("SSE stream error: %s", e.getMessage());
            return new ConnectResult(Outcome.ERROR, -1);
        } catch (RuntimeException e) {
            logger.debug("SSE parser or callback error: %s", e.getMessage());
            return new ConnectResult(Outcome.ERROR, -1);
        } finally {
            connection.disconnect();
        }
    }
}
