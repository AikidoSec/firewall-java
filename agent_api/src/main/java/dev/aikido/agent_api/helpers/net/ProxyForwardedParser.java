package dev.aikido.agent_api.helpers.net;

import dev.aikido.agent_api.helpers.env.BooleanEnv;

import java.util.Map;

import static dev.aikido.agent_api.helpers.net.IPAddressRegex.isIPAddress;

public class ProxyForwardedParser {
    private static final String X_FORWARDED_FOR = "x-forwarded-for";

    public static String getIpFromRequest(String rawIp, Map<String, String> headers) {
        String xForwardedForHeader = headers.get(X_FORWARDED_FOR);
        BooleanEnv trustProxy = new BooleanEnv("AIKIDO_TRUST_PROXY", /* default : */ true);
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty() && trustProxy.getValue()) {
            // Parse X-Forwarded-For and return the correct IP :
            String xForwardedForIp = extractIpFromHeader(xForwardedForHeader);
            if (xForwardedForIp != null) {
                return xForwardedForIp;
            }
        }

        // If no valid IP was found, or if X-Forwarded-For was not present, default to raw ip:
        return rawIp;
    }
    private static String extractIpFromHeader(String xForwardedForHeader) {
        String[] ips = xForwardedForHeader.split(",");
        for (String ip: ips) {
            ip = ip.trim();

            // Some proxies pass along port numbers inside x-forwarded-for :
            if (ip.contains(":")) {
                String[] ipParts = ip.split(":");
                if (ipParts.length == 2 && isIPAddress(ipParts[0])) {
                    return ipParts[0];
                }
            }

            // Continue to check the IPs :
            if (isIPAddress(ip)) {
                return ip;
            }
        }
        return null;
    }
}
