package dev.aikido.agent_api.storage;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * This class holds all config objects from Aikido's servers, i.e. endpoints, blocked IPs, bypassed users, ...
 * It is essential for e.g. rate limiting
 */
public class ServiceConfiguration {
    public ReportingApi.APIListsResponse blockedListsRes = null;
    private boolean blockingEnabled;
    private boolean receivedAnyStats;
    private boolean middlewareInstalled;
    private HashSet<String> bypassedIPs = new HashSet<>();
    private HashSet<String> blockedUserIDs = new HashSet<>();
    private List<Endpoint> endpoints = new ArrayList<>();

    public ServiceConfiguration() {
        this.receivedAnyStats = true; // true by default, waiting for the startup event
        this.middlewareInstalled = false; // false by default, since it has to be set to true by middleware
    }

    public void updateConfig(APIResponse apiResponse) {
        if (apiResponse == null || !apiResponse.success()) {
            return;
        }
        this.blockingEnabled = apiResponse.block();
        if (apiResponse.allowedIPAddresses() != null) {
            this.bypassedIPs = new HashSet<>(apiResponse.allowedIPAddresses());
        }
        if (apiResponse.blockedUserIds() != null) {
            this.blockedUserIDs = new HashSet<>(apiResponse.blockedUserIds());
        }
        if (apiResponse.endpoints() != null) {
            this.endpoints = apiResponse.endpoints();
        }
        this.receivedAnyStats = apiResponse.receivedAnyStats();
    }

    public boolean isBlockingEnabled() {
        return blockingEnabled;
    }

    public void setBlocking(boolean block) {
        this.blockingEnabled = block;
    }

    public boolean hasReceivedAnyStats() {
        return receivedAnyStats;
    }

    public boolean isMiddlewareInstalled() {
        return middlewareInstalled;
    }

    public void setMiddlewareInstalled(boolean middlewareInstalled) {
        this.middlewareInstalled = middlewareInstalled;
    }

    public HashSet<String> getBypassedIPs() {
        return bypassedIPs;
    }

    public List<Endpoint> getEndpoints() {
        return endpoints;
    }

    public HashSet<String> getBlockedUserIDs() {
        return blockedUserIDs;
    }

    public void storeBlockedListsRes(Optional<ReportingApi.APIListsResponse> apiListsResponse) {
        if (apiListsResponse.isPresent()) {
            this.blockedListsRes = apiListsResponse.get();
        }
    }
}
