package dev.aikido.agent_api.storage;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.helpers.net.IPList;
import dev.aikido.agent_api.storage.service_configuration.Domain;
import dev.aikido.agent_api.storage.service_configuration.ParsedFirewallLists;
import dev.aikido.agent_api.storage.statistics.StatisticsStore;

import java.util.*;

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
    private Map<String, Domain> domains = new HashMap<>();
    private boolean blockNewOutgoingRequests = false;

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
        if (apiResponse.domains() != null) {
            for (Domain domain : apiResponse.domains()) {
                if (this.domains.get(domain.hostname()) != null) {
                    continue; // use first provided domain value
                }
                this.domains.put(domain.hostname(), domain);
            }
        }
        this.blockNewOutgoingRequests = apiResponse.blockNewOutgoingRequests();
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
        // Check for allowed ip addresses (i.e. only one country is allowed to visit the site)
        // Always allow access from private IP addresses (those include local IP addresses)
        if (!isPrivateIp(ip) && !firewallLists.matchesAllowedIps(ip)) {
            return new BlockedResult(true, "not in allowlist");
        }

        // Check for monitored IP addresses
        List<ParsedFirewallLists.Match> monitoredIpMatches = firewallLists.matchMonitoredIps(ip);
        for (ParsedFirewallLists.Match monitoredMatch: monitoredIpMatches) {
            StatisticsStore.incrementIpHits(monitoredMatch.key());
        }

        // Check for blocked IP addresses
        List<ParsedFirewallLists.Match> blockedIpMatches = firewallLists.matchBlockedIps(ip);
        for (ParsedFirewallLists.Match blockedMatch : blockedIpMatches) {
            StatisticsStore.incrementIpHits(blockedMatch.key());
        }
        if (!blockedIpMatches.isEmpty()) {
            String description = blockedIpMatches.get(0).description();
            return new BlockedResult(true, description);
        }

        return new BlockedResult(false, null);
    }

    public void updateBlockedLists(ReportingApi.APIListsResponse res) {
        this.firewallLists.update(res);
    }

    /**
     * Check if a given User-Agent is blocked or not :
     */
    public boolean isBlockedUserAgent(String userAgent) {
        ParsedFirewallLists.UABlockedResult result = this.firewallLists.matchBlockedUserAgents(userAgent);
        for (String matchedKey : result.matchedKeys()) {
            StatisticsStore.incrementUAHits(matchedKey);
        }
        return result.block();
    }

    public record BlockedResult(boolean blocked, String description) {
    }

    public boolean shouldBlockOutgoingRequest(String hostname) {
        Domain matchingDomain = this.domains.get(hostname);
        if (matchingDomain == null) {
            return false;
        }

        boolean isDomainBlocked = matchingDomain.isBlockingMode();
        if (this.blockNewOutgoingRequests) {
            return isDomainBlocked;
        }

        return isDomainBlocked;
    }
}
