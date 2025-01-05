package dev.aikido.agent_api.background;

import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.helpers.net.BlockList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * This class holds all config objects from Aikido's servers, i.e. endpoints, blocked IPs, bypassed users, ...
 * It is essential for e.g. rate limiting
 */
public class ServiceConfiguration {
    public record BlockedIpEntry(BlockList blocklist, String description) {}
    private final String serverless;
    private boolean blockingEnabled;
    private boolean receivedAnyStats;
    private HashSet<String> bypassedIPs =  new HashSet<>();
    private HashSet<String> blockedUserIDs = new HashSet<>();
    private List<Endpoint> endpoints = new ArrayList<>();
    private List<BlockedIpEntry> blockedIps = new ArrayList<>();
    public ServiceConfiguration(boolean blockingEnabled, String serverless) {
        if (serverless != null && serverless.isEmpty()) {
            throw new IllegalArgumentException("Serverless cannot be an empty string");
        }
        this.blockingEnabled = blockingEnabled;
        this.serverless = serverless;
        // This is true by default, awaiting the startup event, if the startup event is unsuccessfull this will remain true.
        this.receivedAnyStats = true;
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
    public HashSet<String> getBypassedIPs() {
        return bypassedIPs;
    }
    public List<Endpoint> getEndpoints() {
        return endpoints;
    }
    public HashSet<String> getBlockedUserIDs() {
        return blockedUserIDs;
    }

    /**
     * Check if the IP is blocked (e.g. Geo IP Restrictions)
     */
    public BlockedResult isIpBlocked(String ip) {
        for (BlockedIpEntry entry: blockedIps) {
            if (entry.blocklist.isBlocked(ip)) {
                return new BlockedResult(true, entry.description);
            }
        }
        return new BlockedResult(false, null);
    }
    public record BlockedResult(boolean blocked, String description) {}
    public void updateBlockedIps(Optional<ReportingApi.APIListsResponse> apiListsResponse) {
        if (!apiListsResponse.isEmpty()) {
            ReportingApi.APIListsResponse res = apiListsResponse.get();
            for (ReportingApi.ListsResponseEntry entry : res.blockedIPAddresses()) {
                BlockList blockList = new BlockList();
                for (String ip : entry.ips()) {
                    blockList.add(ip);
                }
                blockedIps.add(new BlockedIpEntry(blockList, entry.description()));
            }
        }
    }
}
