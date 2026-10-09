package dev.aikido.agent_api.background.cloud;

import dev.aikido.agent_api.Config;
import dev.aikido.agent_api.helpers.UUIDv7;
import dev.aikido.agent_api.helpers.net.IPAddress;
import java.util.Map;

public final class AgentHeaders {
    private AgentHeaders() {}

    private static final String SESSION_ID = UUIDv7.generate().toString();

    public static Map<String, String> get() {
        return Map.ofEntries(
                Map.entry("X-Agent-Platform", "java"),
                Map.entry("X-Agent-Library", "firewall-java"),
                Map.entry("X-Agent-Version", Config.pkgVersion),
                Map.entry("X-Agent-Hostname", GetManagerInfo.getHostname()),
                Map.entry("X-Agent-IP-Address", IPAddress.get()),
                Map.entry("X-Agent-Session-Id", SESSION_ID));
    }
}
