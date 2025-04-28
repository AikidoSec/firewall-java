package dev.aikido.agent_api.storage.service_configuration;

import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.helpers.net.IPList;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static dev.aikido.agent_api.helpers.IPListBuilder.createIPList;

public class ParsedFirewallLists {
    private final List<IPEntry> blockedIps = new ArrayList<>();
    private final List<IPEntry> allowedIps = new ArrayList<>();
    private final List<BlockedUAEntry> blockedUserAgents = new ArrayList<>();

    public ParsedFirewallLists() {

    }

    private static List<Match> matchIpEntries(String ip, List<IPEntry> ipEntries) {
        List<Match> matches = new ArrayList<>();
        for (IPEntry entry : ipEntries) {
            if (entry.ips().matches(ip)) {
                matches.add(new Match(entry.key(), !entry.monitor(), entry.description()));
            }
        }
        return matches;
    }

    public List<Match> matchBlockedIps(String ip) {
        List<Match> matches = new ArrayList<>();
        for (IPEntry entry : this.blockedIps) {
            if (entry.ips().matches(ip)) {
                matches.add(new Match(entry.key(), !entry.monitor(), entry.description()));
            }
        }
        return matches;
    }

    // returns true if one or more matches has been found with allowlist.
    public boolean matchesAllowedIps(String ip) {
        for (IPEntry entry : this.allowedIps) {
            if (entry.ips().matches(ip)) {
                return true;
            }
        }
        return false;
    }

    public List<Match> matchBlockedUserAgents(String userAgent) {
        List<Match> matches = new ArrayList<>();
        for (BlockedUAEntry entry : this.blockedUserAgents) {
            if (entry.pattern().matcher(userAgent).find()) {
                matches.add(new Match(entry.key(), !entry.monitor(), null));
            }
        }
        return matches;
    }

    public void update(ReportingApi.APIListsResponse response) {
        updateBlockedIps(response.blockedIPAddresses());
        updateAllowedIps(response.allowedIPAddresses());
        updateBlockedUserAgents(response.blockedUserAgents());
    }

    public void updateBlockedIps(List<ReportingApi.ListsResponseEntry> blockedIpsList) {
        blockedIps.clear();
        if (blockedIpsList == null)
            return;
        for (ReportingApi.ListsResponseEntry entry : blockedIpsList) {
            IPList ipList = createIPList(entry.ips());
            blockedIps.add(new IPEntry(entry.monitor(), entry.key(), entry.source(), entry.description(), ipList));
        }
    }

    public void updateAllowedIps(List<ReportingApi.ListsResponseEntry> allowedIpsList) {
        allowedIps.clear();
        if (allowedIpsList == null)
            return;
        for (ReportingApi.ListsResponseEntry entry : allowedIpsList) {
            IPList ipList = createIPList(entry.ips());
            allowedIps.add(new IPEntry(entry.monitor(), entry.key(), entry.source(), entry.description(), ipList));
        }
    }

    public void updateBlockedUserAgents(List<ReportingApi.BotBlocklist> blockedUserAgentsList) {
        blockedUserAgents.clear();
        if (blockedUserAgentsList == null)
            return;
        for (ReportingApi.BotBlocklist entry : blockedUserAgentsList) {
            Pattern pattern = Pattern.compile(entry.pattern(), Pattern.CASE_INSENSITIVE);
            blockedUserAgents.add(new BlockedUAEntry(entry.monitor(), entry.key(), pattern));
        }
    }

    public record Match(String key, boolean block, String description) {
    }

    private record IPEntry(boolean monitor, String key, String source, String description, IPList ips) {
    }

    private record BlockedUAEntry(boolean monitor, String key, Pattern pattern) {
    }
}
