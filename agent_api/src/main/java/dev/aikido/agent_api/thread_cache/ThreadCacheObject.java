package dev.aikido.agent_api.thread_cache;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.helpers.net.BlockList;
import dev.aikido.agent_api.storage.Hostnames;
import dev.aikido.agent_api.storage.routes.Routes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;

public class ThreadCacheObject {
    private final List<Endpoint> endpoints;
    private final Set<String> blockedUserIds;
    private final Set<String> bypassedIPs;
    private final long lastRenewedAtMS;
    private final Hostnames hostnames;
    private final Routes routes;
    public record BlockedIpEntry(BlockList blocklist, String description) {}
    private List<BlockedIpEntry> blockedIps = new ArrayList<>();
    public ThreadCacheObject(List<Endpoint> endpoints, Set<String> blockedUserIDs, Set<String> bypassedIPs, Routes routes, Optional<ReportingApi.APIListsResponse> blockedListsRes) {
        this.lastRenewedAtMS = getUnixTimeMS();
        // Set endpoints :
        this.endpoints = endpoints;
        this.blockedUserIds = blockedUserIDs;
        this.bypassedIPs = bypassedIPs;
        this.routes = routes;
        this.hostnames = new Hostnames(5000);
        this.updateBlockedLists(blockedListsRes);
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
    public boolean isBypassedIP(String ip) {
        return bypassedIPs.contains(ip);
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
    public void updateBlockedLists(Optional<ReportingApi.APIListsResponse> blockedListsRes) {
        if (!blockedListsRes.isEmpty()) {
            ReportingApi.APIListsResponse res = blockedListsRes.get();
            // Update blocked IP addresses (e.g. for geo restrictions) :
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
