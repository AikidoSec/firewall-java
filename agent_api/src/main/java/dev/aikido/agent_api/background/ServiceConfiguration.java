package dev.aikido.agent_api.background;

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
    private final String serverless;
    private boolean blockingEnabled;
    private boolean receivedAnyStats;
    private boolean middlewareInstalled;
    private HashSet<String> bypassedIPs =  new HashSet<>();
    private HashSet<String> blockedUserIDs = new HashSet<>();
    private List<Endpoint> endpoints = new ArrayList<>();
    public ReportingApi.APIListsResponse blockedListsRes = null;
    public ServiceConfiguration(boolean blockingEnabled, String serverless) {
        if (serverless != null && serverless.isEmpty()) {
            throw new IllegalArgumentException("Serverless cannot be an empty string");
        }
        this.blockingEnabled = blockingEnabled;
        this.serverless = serverless;
        // This is true by default, awaiting the startup event, if the startup event is unsuccessfull this will remain true.
        this.receivedAnyStats = true;
        this.middlewareInstalled = false;
    }
    public void updateConfig(APIResponse apiResponse) {
        if (apiResponse == null || !apiResponse.success()) {
            return;
        }
        this.blockingEnabled = apiResponse.block();
        if (apiResponse.allowedIPAddresses() != null) {
            this.bypassedIPs = new HashSet<>(apiResponse.allowedIPAddresses());
        } if (apiResponse.blockedUserIds() != null) {
            this.blockedUserIDs = new HashSet<>(apiResponse.blockedUserIds());
        } if (apiResponse.endpoints() != null) {
            this.endpoints = apiResponse.endpoints();
        }
        this.receivedAnyStats = apiResponse.receivedAnyStats();
    }
    // Getters :
    public String getServerless() {
        return serverless;
    }
    public boolean isBlockingEnabled() {
        return blockingEnabled;
    }
    public boolean hasReceivedAnyStats() {return receivedAnyStats; }
    public boolean isMiddlewareInstalled() { return middlewareInstalled; }
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
    public void setMiddlewareInstalled() {
        this.middlewareInstalled = true;
    }
}
