package dev.aikido.agent_api.thread_cache;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.storage.Hostnames;
import dev.aikido.agent_api.storage.routes.Routes;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;

public class ThreadCacheObject {
    private final long lastRenewedAtMS;
    private final Hostnames hostnames;

    public ThreadCacheObject(List<Endpoint> endpoints, Set<String> blockedUserIDs, Set<String> bypassedIPs, Routes routes, Optional<ReportingApi.APIListsResponse> blockedListsRes) {
        this.lastRenewedAtMS = getUnixTimeMS();
        this.hostnames = new Hostnames(5000);
    }

    public long getLastRenewedAtMS() {
        return lastRenewedAtMS;
    }
    public Hostnames getHostnames() {
        return hostnames;
    }
}
