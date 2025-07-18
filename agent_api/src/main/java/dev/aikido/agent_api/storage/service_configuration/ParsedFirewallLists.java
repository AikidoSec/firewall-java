package dev.aikido.agent_api.storage.service_configuration;

import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.helpers.net.IPList;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static dev.aikido.agent_api.helpers.IPListBuilder.createIPList;
import static dev.aikido.agent_api.helpers.patterns.SafePatternCompiler.compilePatternSafely;

public class ParsedFirewallLists {
    private final List<IPEntry> blockedIps = new ArrayList<>();
    private final List<IPEntry> monitoredIps = new ArrayList<>();
    private final List<IPEntry> allowedIps = new ArrayList<>();
    private final List<UADetailsEntry> uaDetails = new ArrayList<>();
    private Pattern blockedUserAgents = null;
    private Pattern monitoredUserAgents = null;

    public ParsedFirewallLists() {

    }

    public List<Match> matchBlockedIps(String ip) {
        List<Match> matches = new ArrayList<>();
        for (IPEntry entry : this.blockedIps) {
            if (entry.ips().matches(ip)) {
                matches.add(new Match(entry.key(), entry.description()));
            }
        }
        return matches;
    }

    public List<Match> matchMonitoredIps(String ip) {
        List<Match> matches = new ArrayList<>();
        for (IPEntry entry : this.monitoredIps) {
            if (entry.ips().matches(ip)) {
                matches.add(new Match(entry.key(), entry.description()));
            }
        }
        return matches;
    }


    // returns true if one or more matches has been found with allowlist.
    public boolean matchesAllowedIps(String ip) {
        if (this.allowedIps.isEmpty()) {
            return true; // Empty allowed is means all ips match
        }
        for (IPEntry entry : this.allowedIps) {
            if (entry.ips().matches(ip)) {
                return true;
            }
        }
        return false;
    }

    public UABlockedResult matchBlockedUserAgents(String userAgent) {
        boolean isBlocked = false;
        if (blockedUserAgents != null)
            isBlocked = blockedUserAgents.matcher(userAgent).find();

        boolean isMonitored = false;
        if (monitoredUserAgents != null)
            isMonitored = monitoredUserAgents.matcher(userAgent).find();

        if (!isMonitored && !isBlocked)
            // only run the more detailed matches if it's an actual attack/monitored.
            return new UABlockedResult(false, List.of());

        List<String> matchedUAKeys = new ArrayList<>();
        for (UADetailsEntry entry : this.uaDetails) {
            if (entry.pattern().matcher(userAgent).find()) {
                matchedUAKeys.add(entry.key());
            }
        }
        return new UABlockedResult(isBlocked, matchedUAKeys);
    }

    public void update(ReportingApi.APIListsResponse response) {
        updateBlockedIps(response.blockedIPAddresses());
        updateMonitoredIps(response.monitoredIPAddresses());
        updateAllowedIps(response.allowedIPAddresses());
        updateBlockedAndMonitoredUAs(response.blockedUserAgents(), response.monitoredUserAgents());
        updateUADetails(response.userAgentDetails());
    }

    public void updateBlockedIps(List<ReportingApi.ListsResponseEntry> blockedIpLists) {
        this.blockedIps.clear();
        if (blockedIpLists == null)
            return;
        for (ReportingApi.ListsResponseEntry entry : blockedIpLists) {
            IPList ipList = createIPList(entry.ips());
            this.blockedIps.add(new IPEntry(entry.key(), entry.source(), entry.description(), ipList));
        }
    }

    public void updateMonitoredIps(List<ReportingApi.ListsResponseEntry> monitoredIpsList) {
        this.monitoredIps.clear();
        if (monitoredIpsList == null)
            return;
        for (ReportingApi.ListsResponseEntry entry : monitoredIpsList) {
            IPList ipList = createIPList(entry.ips());
            this.monitoredIps.add(new IPEntry(entry.key(), entry.source(), entry.description(), ipList));
        }
    }

    public void updateAllowedIps(List<ReportingApi.ListsResponseEntry> allowedIpLists) {
        allowedIps.clear();
        if (allowedIpLists == null)
            return;
        for (ReportingApi.ListsResponseEntry entry : allowedIpLists) {
            IPList ipList = createIPList(entry.ips());
            allowedIps.add(new IPEntry(entry.key(), entry.source(), entry.description(), ipList));
        }
    }

    public void updateUADetails(List<ReportingApi.UserAgentDetail> userAgentDetails) {
        this.uaDetails.clear();
        if (userAgentDetails == null)
            return;
        for (ReportingApi.UserAgentDetail entry : userAgentDetails) {
            Pattern pattern = compilePatternSafely(entry.pattern(), Pattern.CASE_INSENSITIVE);
            if (pattern != null) {
                this.uaDetails.add(new UADetailsEntry(entry.key(), pattern));
            }
        }
    }

    public void updateBlockedAndMonitoredUAs(String blockedUAs, String monitoredUAs) {
        this.blockedUserAgents = compilePatternSafely(blockedUAs, Pattern.CASE_INSENSITIVE);
        this.monitoredUserAgents = compilePatternSafely(monitoredUAs, Pattern.CASE_INSENSITIVE);
    }


    public record Match(String key, String description) {
    }

    public record UABlockedResult(boolean block, List<String> matchedKeys) {
    }

    private record IPEntry(String key, String source, String description, IPList ips) {
    }

    private record UADetailsEntry(String key, Pattern pattern) {
    }
}
