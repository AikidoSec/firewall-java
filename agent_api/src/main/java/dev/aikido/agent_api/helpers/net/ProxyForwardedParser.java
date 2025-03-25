package dev.aikido.agent_api.helpers.net;

import dev.aikido.agent_api.helpers.env.BooleanEnv;

import java.util.Map;

public class ProxyForwardedParser {
    private static final String X_FORWARDED_FOR = "x-forwarded-for";

    public static String getIpFromRequest(String rawIp, String xForwardedForHeader) {
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty() && trustProxy()) {
            // Parse X-Forwarded-For and return the correct IP :
            String xForwardedForIp = extractIpFromHeader(xForwardedForHeader);
            if (xForwardedForIp != null) {
                return xForwardedForIp;
            }
        }

        // If no valid IP was found, or if X-Forwarded-For was not present, default to raw ip:
        return rawIp;
    }

    /**
     * Checks the boolean environment variable `AIKIDO_TRUST_PROXY`, default is true.
     */
    public static boolean trustProxy() {
        BooleanEnv trustProxy = new BooleanEnv("AIKIDO_TRUST_PROXY", /* default : */ true);
        return trustProxy.getValue();
    }

    private static String extractIpFromHeader(String xForwardedForHeader) {
        String[] ips = xForwardedForHeader.split(",");
        for (String ip: ips) {
            ip = ip.trim();

            // Some proxies pass along port numbers inside x-forwarded-for :
            if (ip.contains(":")) {
                String[] ipParts = ip.split(":");
                if (ipParts.length == 2 && IPValidator.isIP(ipParts[0])) {
                    return ipParts[0];
                }
            }

            // Continue to check the IPs :
            if (IPValidator.isIP(ip)) {
                return ip;
            }
        }
        return null;
    }
}
