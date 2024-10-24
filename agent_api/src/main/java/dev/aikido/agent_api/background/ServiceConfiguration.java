package dev.aikido.agent_api.background;

import dev.aikido.agent_api.background.cloud.api.APIResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * This class holds all config objects from Aikido's servers, i.e. endpoints, blocked IPs, bypassed users, ...
 * It is essential for e.g. rate limiting
 */
public class ServiceConfiguration {
    private final String serverless;
    private boolean blockingEnabled;
    private HashSet<String> bypassedIPs =  new HashSet<>();
    private HashSet<String> blockedUserIDs = new HashSet<>();
    private List<Endpoint> endpoints = new ArrayList<>();
    public ServiceConfiguration(boolean blockingEnabled, String serverless) {
        if (serverless != null && serverless.isEmpty()) {
            throw new IllegalArgumentException("Serverless cannot be an empty string");
        }
        this.blockingEnabled = blockingEnabled;
        this.serverless = serverless;

    }
    public void updateConfig(APIResponse apiResponse) {
        if (apiResponse == null || !apiResponse.success()) {
            return;
        }
        this.blockingEnabled = apiResponse.block();
        this.bypassedIPs = new HashSet<>(apiResponse.allowedIPAddresses());
        this.blockedUserIDs = new HashSet<>(apiResponse.blockedUserIds());
        this.endpoints = apiResponse.endpoints();
    }
    // Getters :
    public String getServerless() {
        return serverless;
    }
    public boolean isBlockingEnabled() {
        return blockingEnabled;
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
}
