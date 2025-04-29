package dev.aikido.agent_api.storage;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.helpers.net.IPList;
import dev.aikido.agent_api.storage.service_configuration.ParsedFirewallLists;
import dev.aikido.agent_api.storage.statistics.StatisticsStore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static dev.aikido.agent_api.helpers.IPListBuilder.createIPList;
import static dev.aikido.agent_api.vulnerabilities.ssrf.IsPrivateIP.isPrivateIp;

/**
 * This class holds all config objects from Aikido's servers, i.e. endpoints, blocked IPs, bypassed users, ...
 * It is essential for e.g. rate limiting
 */
public class ServiceConfiguration {
    private final ParsedFirewallLists firewallLists = new ParsedFirewallLists();
    private boolean blockingEnabled;
    private boolean receivedAnyStats;
    private boolean middlewareInstalled;
    private IPList bypassedIPs = new IPList();
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
            this.bypassedIPs = createIPList(apiResponse.allowedIPAddresses());
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

    public List<Endpoint> getEndpoints() {
        return endpoints;
    }

    public boolean isUserBlocked(String userId) {
        return this.blockedUserIDs.contains(userId);
    }

    public boolean isIpBypassed(String ip) {
        return this.bypassedIPs.matches(ip);
    }

    /**
     * Check if the IP is blocked (e.g. Geo IP Restrictions)
     */
    public BlockedResult isIpBlocked(String ip) {
        BlockedResult blockedResult = new BlockedResult(false, null);

        // Check for allowed ip addresses (i.e. only one country is allowed to visit the site)
        // Always allow access from private IP addresses (those include local IP addresses)
        if (!isPrivateIp(ip) && !firewallLists.matchesAllowedIps(ip)) {
            blockedResult = new BlockedResult(true, "not in allowlist");
        }

        // Check for blocked ip addresses
        for (ParsedFirewallLists.Match match : firewallLists.matchBlockedIps(ip)) {
            StatisticsStore.incrementIpHits(match.key(), match.block());
            // when a blocking match is found, set blocked result if it hasn't been set already.
            if (match.block() && !blockedResult.blocked()) {
                blockedResult = new BlockedResult(true, match.description());
            }
        }

        return blockedResult;
    }

    public void updateBlockedLists(ReportingApi.APIListsResponse res) {
        this.firewallLists.update(res);
    }

    /**
     * Check if a given User-Agent is blocked or not :
     */
    public boolean isBlockedUserAgent(String userAgent) {
        boolean blocked = false;
        for (ParsedFirewallLists.Match match : this.firewallLists.matchBlockedUserAgents(userAgent)) {
            StatisticsStore.incrementUAHits(match.key(), match.block());
            if (match.block()) {
                blocked = true;
                break;
            }
        }

        return blocked;
    }

    // IP restrictions (e.g. Geo-IP Restrictions) :
    public record IPListEntry(IPList ipList, String description, boolean monitor, String key) {
    }

    public record BlockedResult(boolean blocked, String description) {
    }
}
