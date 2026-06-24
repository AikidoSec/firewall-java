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

        // No (trusted) forwarding header: fall back to the raw socket address.
        // Some servers/proxies hand us "ip:port" here too, so strip the port as
        // well, otherwise the port ends up in attacks, rate limiting and the
        // reported user IPs (which causes duplicate IPs on the cloud side).
        return stripPort(rawIp);
    }

    /**
     * Checks the boolean environment variable `AIKIDO_TRUST_PROXY`, default is true.
     */
    public static boolean trustProxy() {
        BooleanEnv trustProxy = new BooleanEnv("AIKIDO_TRUST_PROXY", /* default : */ true);
        return trustProxy.getValue();
    }

    /**
     * Strips a trailing port from an IP address, returning the bare IP. Handles
     * IPv4 with a port ("1.2.3.4:5678") and bracketed IPv6 with a port
     * ("[2001:db8::1]:5678"). Already-valid IPs (including bare IPv6) and values
     * from which no port can be safely removed are returned unchanged.
     */
    public static String stripPort(String ipAddress) {
        if (ipAddress == null) {
            return null;
        }
        String ip = ipAddress.trim();

        if (IPValidator.isIP(ip)) {
            return ip;
        }

        // Bracketed IPv6 with optional port, e.g. "[2001:db8::1]:5678".
        if (ip.startsWith("[")) {
            int closing = ip.indexOf(']');
            if (closing > 1) {
                String inner = ip.substring(1, closing);
                if (IPValidator.isIP(inner, "6")) {
                    return inner;
                }
            }
            return ip;
        }

        // IPv4 with a port, e.g. "1.2.3.4:5678". A single colon distinguishes this
        // from a bare IPv6 address, which always has multiple colons.
        int firstColon = ip.indexOf(':');
        if (firstColon > -1 && ip.indexOf(':', firstColon + 1) == -1) {
            String candidate = ip.substring(0, firstColon);
            if (IPValidator.isIP(candidate, "4")) {
                return candidate;
            }
        }

        return ip;
    }

    private static String extractIpFromHeader(String xForwardedForHeader) {
        String[] ips = xForwardedForHeader.split(",");
        for (String ip: ips) {
            // Some proxies pass along port numbers inside x-forwarded-for, so
            // strip them before validating.
            String cleaned = stripPort(ip.trim());
            if (IPValidator.isIP(cleaned)) {
                return cleaned;
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
