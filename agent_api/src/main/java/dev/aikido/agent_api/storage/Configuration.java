package dev.aikido.agent_api.storage;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.helpers.net.IPList;

import java.util.*;
import java.util.regex.Pattern;

import static dev.aikido.agent_api.helpers.IPListBuilder.createIPList;

public class Configuration {
    private boolean blocking;
    private boolean receivedAnyStats = true;
    private boolean middlewareInstalled = false;
    private List<Endpoint> endpoints = new ArrayList<>();
    private Set<String> blockedUserIds = new HashSet<>();
    private IPList bypassedIPs = new IPList();

    // IP restrictions (e.g. Geo-IP Restrictions) :
    public record IPListEntry(IPList ipList, String description) {
    }

    private final List<IPListEntry> blockedIps = new ArrayList<>();
    private final List<IPListEntry> allowedIps = new ArrayList<>();
    // User-Agent Blocking (e.g. bot blocking) :
    private Pattern blockedUserAgentRegex;

    public Configuration() {
    }

    // allowedIps, blockedIps, blockedUserAgentRegex are updated using this function
    public void updateBlockedLists(Optional<ReportingApi.APIListsResponse> blockedListsRes) {
        if (blockedListsRes.isPresent()) {
            ReportingApi.APIListsResponse res = blockedListsRes.get();
            // Update blocked IP addresses (e.g. for geo restrictions) :
            this.blockedIps.clear(); // reset
            if (res.blockedIPAddresses() != null) {
                for (ReportingApi.ListsResponseEntry entry : res.blockedIPAddresses()) {
                    IPList ipList = createIPList(entry.ips());
                    this.blockedIps.add(new IPListEntry(ipList, entry.description()));
                }
            }
            // Update allowed IP addresses (e.g. for geo restrictions) :
            this.allowedIps.clear(); // reset
            if (res.allowedIPAddresses() != null) {
                for (ReportingApi.ListsResponseEntry entry : res.allowedIPAddresses()) {
                    IPList ipList = createIPList(entry.ips());
                    this.allowedIps.add(new IPListEntry(ipList, entry.description()));
                }
            }
            // Update Blocked User-Agents regex
            this.blockedUserAgentRegex = null; // reset
            if (res.blockedUserAgents() != null && !res.blockedUserAgents().isEmpty()) {
                this.blockedUserAgentRegex = Pattern.compile(res.blockedUserAgents(), Pattern.CASE_INSENSITIVE);
            }
        }
    }

    // blocking, endpoints, bypassed ips, blocked user ids are updated using this function
    public void updateConfig(APIResponse apiResponse) {
        if (apiResponse == null || !apiResponse.success()) {
            return;
        }
        this.setBlocking(apiResponse.block());
        this.setBypassedIPs(apiResponse.allowedIPAddresses());
        this.setBlockedUserIds(apiResponse.blockedUserIds());
        this.setEndpoints(apiResponse.endpoints());
        this.setReceivedAnyStats(apiResponse.receivedAnyStats());
    }

    // Blocking status, i.e. should an attack be throwing an error or not
    public boolean isBlockingEnabled() {
        return blocking;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    // blocked user ids: These are users that were blocked in the cloud portal
    public void setBlockedUserIds(List<String> blockedUserIds) {
        if (blockedUserIds != null) {
            this.blockedUserIds = new HashSet<>(blockedUserIds);
        }
    }

    public boolean isUserBlocked(String userId) {
        return this.blockedUserIds.contains(userId);
    }

    // bypassed ips: This IPList contains all IPs that should not be scanned, blocked, etc.
    public void setBypassedIPs(List<String> bypassedIps) {
        this.bypassedIPs = createIPList(bypassedIps);
    }

    public boolean isBypassedIP(String ip) {
        return this.bypassedIPs.matches(ip);
    }

    // endpoints: contains rate-limiting data, allowed ips, ...
    public void setEndpoints(List<Endpoint> endpoints) {
        if (endpoints != null) {
            this.endpoints = endpoints;
        }
    }

    public List<Endpoint> getEndpoints() {
        return this.endpoints;
    }


    public record BlockedResult(boolean blocked, String description) {
    }

    /**
     * Check if the IP is blocked (e.g. Geo IP Restrictions)
     * This first checks the global ip allowlist, and afterward the global ip blocklist.
     */
    public BlockedResult isIpBlocked(String ip) {
        for (IPListEntry entry : allowedIps) {
            if (!entry.ipList.matches(ip)) {
                return new BlockedResult(true, entry.description);
            }
        }
        for (IPListEntry entry : blockedIps) {
            if (entry.ipList.matches(ip)) {
                return new BlockedResult(true, entry.description);
            }
        }
        return new BlockedResult(false, null);
    }

    /**
     * Check if a given User-Agent is blocked or not
     */
    public boolean isBlockedUserAgent(String userAgent) {
        if (blockedUserAgentRegex != null) {
            return blockedUserAgentRegex.matcher(userAgent).find();
        }
        return false;
    }

    // receivedAnyStats: true by default, awaiting the startup event, if the startup
    // event is unsuccessful this will remain true.
    public void setReceivedAnyStats(boolean receivedAnyStats) {
        this.receivedAnyStats = receivedAnyStats;
    }

    public boolean hasReceivedAnyStats() {
        return this.receivedAnyStats;
    }

    // middlewareInstalled: indicates that the shouldBlockRequest function was called
    public void setMiddlewareInstalled(boolean middlewareInstalled) {
        this.middlewareInstalled = middlewareInstalled;
    }

    public boolean isMiddlewareInstalled() {
        return this.middlewareInstalled;
    }
}
