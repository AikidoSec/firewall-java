package dev.aikido.agent_api.helpers.net;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class IPAddress {
    private IPAddress() {
    }

    public static String getIpAddress() {
        try {
            String hostAddress = InetAddress.getLocalHost().getHostAddress();

            // Remove the zone index if present
            if (hostAddress.contains("%")) {
                hostAddress = hostAddress.substring(0, hostAddress.indexOf('%'));
            }

            return hostAddress;
        } catch (UnknownHostException ignored) {
            // pass-through
        }
        return "0.0.0.0";
    }
}
