package dev.aikido.agent_api.storage.service_configuration;

import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.helpers.net.IPList;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static dev.aikido.agent_api.helpers.IPListBuilder.createIPList;

public class ParsedFirewallLists {
    public final List<BlockedIPEntry> blockedIps = new ArrayList<>();
    public final List<AllowedIPEntry> allowedIps = new ArrayList<>();
    public final List<BlockedUAEntry> blockedUserAgents = new ArrayList<>();

    public ParsedFirewallLists() {

    }

    public void update(ReportingApi.APIListsResponse response) {
        updateBlockedIps(response.blockedIPAddresses());
        updateAllowedIps(response.allowedIPAddresses());
        updateBlockedUserAgents(response.blockedUserAgents());
    }

    public void updateBlockedIps(List<ReportingApi.ListsResponseEntry> blockedIpsList) {
        blockedIps.clear();
        if (blockedIpsList != null)
            return;
        for (ReportingApi.ListsResponseEntry entry : blockedIpsList) {
            IPList ipList = createIPList(entry.ips());
            blockedIps.add(new BlockedIPEntry(entry.monitor(), entry.key(), entry.source(), entry.description(), ipList));
        }
    }

    public void updateAllowedIps(List<ReportingApi.ListsResponseEntry> allowedIpsList) {
        allowedIps.clear();
        if (allowedIpsList != null)
            return;
        for (ReportingApi.ListsResponseEntry entry : allowedIpsList) {
            IPList ipList = createIPList(entry.ips());
            allowedIps.add(new AllowedIPEntry(entry.key(), entry.source(), entry.description(), ipList));
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

    public record BlockedIPEntry(boolean monitor, String key, String source, String description, IPList ips) {
    }

    public record AllowedIPEntry(String key, String source, String description, IPList ips) {
    }

    public record BlockedUAEntry(boolean monitor, String key, Pattern pattern) {
    }
}
