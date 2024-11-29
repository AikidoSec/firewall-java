package dev.aikido.agent_api.helpers.net;

import java.util.Set;

public final class LocalhostIP {
    private LocalhostIP() {}

    private static final Set<String> localhostIps = Set.of(
        "127.0.0.1",
        "::ffff:127.0.0.1",
        "::1"
    );
    public static boolean isLocalhostIP(String ip) {
        return localhostIps.contains(ip);
    }
}
