package dev.aikido.agent_api.helpers.net;

import static dev.aikido.agent_api.vulnerabilities.ssrf.IsPrivateIP.isPrivateIp;

import java.util.HashMap;
import java.util.List;

import static dev.aikido.agent_api.context.ContextObject.getHeader;

public class ProxyForwardedParser {
    public static String getIpFromRequest(String rawIp, HashMap<String, List<String>> headers) {
        String ipHeader = getHeader(headers, getIpHeaderName());
        TrustProxy.Config config = TrustProxy.getConfig();
        if (ipHeader != null && !ipHeader.isEmpty() && isTrustedProxyRequest(rawIp)) {
            // Parse ip header and return the correct IP :
            String ipHeaderValue = extractIpFromHeader(ipHeader, config);
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

    public static boolean isTrustedProxyRequest(String rawIp) {
        return TrustProxy.isTrustedProxyRequest(normalizeIp(rawIp));
    }

    private static String extractIpFromHeader(String xForwardedForHeader, TrustProxy.Config config) {
        String[] ips = xForwardedForHeader.split(",", -1);
        int startIndex = config instanceof TrustProxy.CountConfig countConfig
                ? ips.length - countConfig.count()
                : ips.length - 1;
        for (int index = startIndex; index >= 0; index--) {
            String normalizedIp = normalizeIp(ips[index].trim());
            if (normalizedIp == null || isPrivateIp(normalizedIp)) {
                continue;
            }
            if (config instanceof TrustProxy.CIDRConfig cidrConfig && cidrConfig.ranges().matches(normalizedIp)) {
                continue;
            }
            return normalizedIp;
        }
        return null;
    }

    private static String normalizeIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return null;
        }

        ip = ip.trim();

        // Some proxies pass along port numbers with IPv4 addresses: ip:port
        if (ip.contains(":")) {
            String[] ipParts = ip.split(":");
            if (ipParts.length == 2 && IPValidator.isIP(ipParts[0], "4")) {
                return ipParts[0];
            }
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

        if (IPValidator.isIP(ip)) {
            return ip;
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
