package dev.aikido.agent_api.background;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.aikido.agent_api.background.cloud.RealtimeSSEAPI;
import dev.aikido.agent_api.background.cloud.SSEParser;
import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.background.cloud.api.ReportingApiHTTP;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import dev.aikido.agent_api.storage.ServiceConfigStore;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class RealtimeSSETask extends Thread {
    private static final Logger logger = LogManager.getLogger(RealtimeSSETask.class);
    private final RealtimeSSEAPI realtimeSSEApi;
    private final ReportingApiHTTP reportingApi;
    private Optional<Long> configLastUpdatedAt;
    private Optional<Long> lastConfigRefreshStartedAt = Optional.empty();

    public RealtimeSSETask(RealtimeSSEAPI realtimeSSEApi, ReportingApiHTTP reportingApi) {
        super("RealtimeSSETask");
        this.realtimeSSEApi = realtimeSSEApi;
        this.reportingApi = reportingApi;
        this.configLastUpdatedAt = Optional.empty();
        setDaemon(true);
    }

    @Override
    public void run() {
        realtimeSSEApi.listen(this::onEvent);
    }

    public synchronized void onEvent(SSEParser.Event event) {
        logger.trace("SSE event received: %s", event.event());
        if (!"config-updated".equals(event.event())) {
            return;
        }

        long configUpdatedAt;
        try {
            JsonObject payload = JsonParser.parseString(event.data()).getAsJsonObject();
            configUpdatedAt = payload.get("configUpdatedAt").getAsLong();
        } catch (RuntimeException e) {
            logger.debug("SSE config-updated event has invalid payload: %s", event.data());
            return;
        }

        if (configLastUpdatedAt.isPresent() && configUpdatedAt <= configLastUpdatedAt.get()) {
            return;
        }

        if (configUpdateArrivedTooFast()) {
            logger.debug("Ignoring SSE config-updated event during refresh throttle");
            return;
        }

        Optional<APIResponse> newConfig = reportingApi.fetchNewConfig();
        if (newConfig.isEmpty()) {
            logger.debug("Failed to fetch config after SSE event");
            return;
        }

        APIResponse config = newConfig.get();
        ServiceConfigStore.updateFromAPIResponse(config);
        configLastUpdatedAt = Optional.of(config.configUpdatedAt());

        Optional<ReportingApi.APIListsResponse> blockedListsRes = reportingApi.fetchBlockedLists();
        blockedListsRes.ifPresent(ServiceConfigStore::updateFromAPIListsResponse);

        logger.debug("Config updated via SSE");
    }

    private boolean configUpdateArrivedTooFast() {
        long now = System.nanoTime();
        if (lastConfigRefreshStartedAt.isPresent()) {
            if (now - lastConfigRefreshStartedAt.get() < TimeUnit.SECONDS.toNanos(9)) {
                return true;
            }
        }

        lastConfigRefreshStartedAt = Optional.of(now);
        return false;
    }
}
