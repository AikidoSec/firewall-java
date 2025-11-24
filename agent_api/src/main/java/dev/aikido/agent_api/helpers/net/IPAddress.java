package dev.aikido.agent_api.helpers.net;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class IPAddress {
    private IPAddress() {
    }

    private static final String IP_ADDRESS;

    static {
        String hostAddress = "0.0.0.0";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
            // Remove the zone index if present
            if (hostAddress.contains("%")) {
                hostAddress = hostAddress.substring(0, hostAddress.indexOf('%'));
            }
        } catch (UnknownHostException ignored) {
            // pass-through
        }
        IP_ADDRESS = hostAddress;
    }

    public static String get() {
        return IP_ADDRESS;
    }
}
