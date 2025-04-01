package dev.aikido.agent_api.storage;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.helpers.net.IPList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import static dev.aikido.agent_api.helpers.IPListBuilder.createIPList;
import static dev.aikido.agent_api.vulnerabilities.ssrf.IsPrivateIP.isPrivateIp;

/**
 * This class holds all config objects from Aikido's servers, i.e. endpoints, blocked IPs, bypassed users, ...
 * It is essential for e.g. rate limiting
 */
public class ServiceConfiguration {
    private final List<IPListEntry> blockedIps = new ArrayList<>();
    private final List<IPListEntry> allowedIps = new ArrayList<>();
    private boolean blockingEnabled;
    private boolean receivedAnyStats;
    private boolean middlewareInstalled;
    private IPList bypassedIPs = new IPList();
    private HashSet<String> blockedUserIDs = new HashSet<>();
    private List<Endpoint> endpoints = new ArrayList<>();
    // User-Agent Blocking (e.g. bot blocking) :
    private Pattern blockedUserAgentRegex;

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
        // Check for allowed ip addresses (i.e. only one country is allowed to visit the site)
        // Always allow access from private IP addresses (those include local IP addresses)
        if (!allowedIps.isEmpty() && !isPrivateIp(ip)) {
            boolean ipAllowed = false;
            for (IPListEntry entry : allowedIps) {
                if (entry.ipList.matches(ip)) {
                    ipAllowed = true; // We allow IP addresses as long as they match with one of the lists.
                    break;
                }
            }
            if (!ipAllowed) {
                return new BlockedResult(true, "not in allowlist");
            }
        }

        // Check for blocked ip addresses
        for (IPListEntry entry : blockedIps) {
            if (entry.ipList.matches(ip)) {
                return new BlockedResult(true, entry.description);
            }
        }
        return new BlockedResult(false, null);
    }

    public void updateBlockedLists(ReportingApi.APIListsResponse res) {
        // clear
        blockedIps.clear();
        allowedIps.clear();
        blockedUserAgentRegex = null;

        // Update blocked IP addresses (e.g. for geo restrictions) :
        if (res.blockedIPAddresses() != null) {
            for (ReportingApi.ListsResponseEntry entry : res.blockedIPAddresses()) {
                IPList ipList = createIPList(entry.ips());
                blockedIps.add(new IPListEntry(ipList, entry.description()));
            }
        }
        // Update allowed IP addresses (e.g. for geo restrictions) :
        if (res.allowedIPAddresses() != null) {
            for (ReportingApi.ListsResponseEntry entry : res.allowedIPAddresses()) {
                IPList ipList = createIPList(entry.ips());
                this.allowedIps.add(new IPListEntry(ipList, entry.description()));
            }
        }
        // Update Blocked User-Agents regex
        if (res.blockedUserAgents() != null && !res.blockedUserAgents().isEmpty()) {
            this.blockedUserAgentRegex = Pattern.compile(res.blockedUserAgents(), Pattern.CASE_INSENSITIVE);
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

    // IP restrictions (e.g. Geo-IP Restrictions) :
    public record IPListEntry(IPList ipList, String description) {
    }

    public record BlockedResult(boolean blocked, String description) {
    }
}
