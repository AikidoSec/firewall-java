package dev.aikido.agent_api.thread_cache;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.helpers.net.IPList;
import dev.aikido.agent_api.storage.Hostnames;
import dev.aikido.agent_api.storage.routes.Routes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static dev.aikido.agent_api.helpers.IPListBuilder.createIPList;
import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;
import static dev.aikido.agent_api.vulnerabilities.ssrf.IsPrivateIP.isPrivateIp;

public class ThreadCacheObject {
    private final List<Endpoint> endpoints;
    private final Set<String> blockedUserIds;
    private final IPList bypassedIPs;
    private final long lastRenewedAtMS;
    private final Hostnames hostnames;
    private final Routes routes;

    // IP restrictions (e.g. Geo-IP Restrictions) :
    public record IPListEntry(IPList ipList, String description) {}
    private List<IPListEntry> blockedIps = new ArrayList<>();
    private List<IPListEntry> allowedIps = new ArrayList<>();
    // User-Agent Blocking (e.g. bot blocking) :
    private Pattern blockedUserAgentRegex;

    private int totalHits = 0;
    private boolean middlewareInstalled = false;
    public ThreadCacheObject(List<Endpoint> endpoints, Set<String> blockedUserIDs, Set<String> bypassedIPs, Routes routes, Optional<ReportingApi.APIListsResponse> blockedListsRes) {
        this.lastRenewedAtMS = getUnixTimeMS();
        // Set endpoints :
        this.endpoints = endpoints;
        this.blockedUserIds = blockedUserIDs;
        this.bypassedIPs = createIPList(bypassedIPs);
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
        return bypassedIPs.matches(ip);
    }

    /**
     * Check if the IP is blocked (e.g. Geo IP Restrictions)
     */
    public BlockedResult isIpBlocked(String ip) {
        // Check for allowed ip addresses (i.e. only one country is allowed to visit the site)
        // Always allow access from private IP addresses (those include local IP addresses)
        if(allowedIps != null && allowedIps.size() > 0 && !isPrivateIp(ip)) {
            boolean ipAllowed = false;
            for (IPListEntry entry: allowedIps) {
                if (entry.ipList.matches(ip)) {
                    ipAllowed = true; // We allow IP addresses as long as they match with one of the lists.
                    break;
                }
            }
            if (!ipAllowed) {
                return new BlockedResult(true, "allowlist");
            }
        }

        // Check for blocked ip addresses
        for (IPListEntry entry: blockedIps) {
            if (entry.ipList.matches(ip)) {
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
            if (res.blockedIPAddresses() != null) {
                for (ReportingApi.ListsResponseEntry entry : res.blockedIPAddresses()) {
                    IPList ipList = createIPList(entry.ips());
                    blockedIps.add(new IPListEntry(ipList, entry.description()));
                }
            }
            // Update allowed IP addresses (e.g. for geo restrictions) :
            if (res.allowedIPAddresses() != null) {
                for (ReportingApi.ListsResponseEntry entry: res.allowedIPAddresses()) {
                    IPList ipList = createIPList(entry.ips());
                    this.allowedIps.add(new IPListEntry(ipList, entry.description()));
                }
            }
            // Update Blocked User-Agents regex
            if (res.blockedUserAgents() != null && !res.blockedUserAgents().isEmpty()) {
                this.blockedUserAgentRegex = Pattern.compile(res.blockedUserAgents(), Pattern.CASE_INSENSITIVE);
            }
        }
    }

    /**
     * Check if a given User-Agent is blocked or not :
     */
    public boolean isBlockedUserAgent(String userAgent) {
        if (blockedUserAgentRegex != null) {
            return blockedUserAgentRegex.matcher(userAgent).find();
        }
        return false;
    }

    public int getTotalHits() { return totalHits; }
    public void incrementTotalHits() {
        this.totalHits += 1;
    }
    public boolean isMiddlewareInstalled() { return middlewareInstalled; }
    public void setMiddlewareInstalled() {
        middlewareInstalled = true;
    }
}
