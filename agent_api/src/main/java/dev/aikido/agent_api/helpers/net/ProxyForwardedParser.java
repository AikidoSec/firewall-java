package dev.aikido.agent_api.helpers.net;

import dev.aikido.agent_api.helpers.env.BooleanEnv;

import java.util.HashMap;
import java.util.List;

import static dev.aikido.agent_api.context.ContextObject.getHeader;
import static dev.aikido.agent_api.helpers.extraction.IPV6BracketsHelper.removeIfExistsIPv6Brackets;

public class ProxyForwardedParser {
    public static String getIpFromRequest(String rawIp, HashMap<String, List<String>> headers) {
        String ip = rawIp;

        String ipHeader = getHeader(headers, getIpHeaderName());
        if (ipHeader != null && !ipHeader.isEmpty() && trustProxy()) {
            // Parse ip header and return the correct IP :
            String ipHeaderValue = extractIpFromHeader(ipHeader);
            if (ipHeaderValue != null) {
                ip = ipHeaderValue;
            }
        }

        // Aikido core cannot handle the [ ] in the request's IP, so we parse them away here :
        ip = removeIfExistsIPv6Brackets(ip);

        return ip;
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

    private static String getIpHeaderName() {
        String clientIpHeader = System.getenv("AIKIDO_CLIENT_IP_HEADER");
        if (clientIpHeader != null && !clientIpHeader.isEmpty()) {
            return clientIpHeader;
        }
        return "X-Forwarded-For";
    }
}
