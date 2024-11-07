package dev.aikido.agent_api.thread_cache;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.storage.Hostnames;
import dev.aikido.agent_api.storage.routes.Routes;

import java.util.List;
import java.util.Set;

import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;

public class ThreadCacheObject {
    private final List<Endpoint> endpoints;
    private final Set<String> blockedUserIds;
    private final long lastRenewedAtMS;
    private final Hostnames hostnames;
    private final Routes routes;
    public ThreadCacheObject(List<Endpoint> endpoints, Set<String> blockedUserIDs, Routes routes) {
        this.lastRenewedAtMS = getUnixTimeMS();
        // Set endpoints :
        this.endpoints = endpoints;
        this.blockedUserIds = blockedUserIDs;
        this.routes = routes;
        this.hostnames = new Hostnames(5000);
    }

    public List<Endpoint> getEndpoints() {
        if (endpoints == null) {
            return List.of();
        }
        return endpoints;
    }
    public boolean isBlockedUserID(String userID) {
        if (blockedUserIds == null) {
            return false;
        }
        return blockedUserIds.contains(userID);
    }

    public long getLastRenewedAtMS() {
        return lastRenewedAtMS;
    }

    public Hostnames getHostnames() {
        return hostnames;
    }
    public Routes getRoutes() {
        return routes;
    }
}
