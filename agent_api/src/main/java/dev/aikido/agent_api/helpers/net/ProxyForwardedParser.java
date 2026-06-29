package dev.aikido.agent_api.helpers.net;

import dev.aikido.agent_api.helpers.env.BooleanEnv;

import java.util.HashMap;
import java.util.List;

import static dev.aikido.agent_api.context.ContextObject.getHeader;

public class ProxyForwardedParser {
    public static String getIpFromRequest(String rawIp, HashMap<String, List<String>> headers) {
        String ipHeader = getHeader(headers, getIpHeaderName());
        if (ipHeader != null && !ipHeader.isEmpty() && trustProxy()) {
            // Parse ip header and return the correct IP :
            String ipHeaderValue = extractIpFromHeader(ipHeader);
            if (ipHeaderValue != null) {
                return ipHeaderValue;
            }
        }

        // If no valid IP was found, or if X-Forwarded-For was not present, default to raw ip:
        String normalizedRawIp = normalizeIp(rawIp);
        if (normalizedRawIp != null) {
            return normalizedRawIp;
        }

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

            String normalizedIp = normalizeIp(ip);
            if (normalizedIp != null) {
                return normalizedIp;
            }
        }
        return null;
    }

    private static String normalizeIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return null;
        }

        ip = ip.trim();

        if (IPValidator.isIP(ip)) {
            return ip;
        }

        if (ip.startsWith("[") && ip.endsWith("]")) {
            String unwrappedIp = ip.substring(1, ip.length() - 1);
            if (IPValidator.isIP(unwrappedIp)) {
                return unwrappedIp;
            }
        }

        if (ip.startsWith("[")) {
            int closingBracket = ip.indexOf("]:");
            if (closingBracket > 0) {
                String unwrappedIp = ip.substring(1, closingBracket);
                if (IPValidator.isIP(unwrappedIp)) {
                    return unwrappedIp;
                }
            }
        }

        // Some proxies pass along port numbers with IP addresses :
        if (ip.contains(":")) {
            String[] ipParts = ip.split(":");
            if (ipParts.length == 2 && IPValidator.isIP(ipParts[0], "4")) {
                return ipParts[0];
            }
        }

        return null;
    }

    private static String getIpHeaderName() {
        String clientIpHeader = System.getenv("AIKIDO_CLIENT_IP_HEADER");
        if (clientIpHeader != null && !clientIpHeader.isEmpty()) {
            return clientIpHeader;
        }
        return "X-Forwarded-For";
    }
}
