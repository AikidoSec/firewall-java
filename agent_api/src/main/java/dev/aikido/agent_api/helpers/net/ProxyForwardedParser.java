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
            String[] ips = xForwardedForHeader.split(",");
            for (String ip : ips) {
                if (isIPAddress(ip.trim())) {
                    return ip.trim(); // Return the first valid IP found
                }
            }
        }

        // If no valid IP was found, or if X-Forwarded-For was not present, default to raw ip:
        return rawIp;
    }
}
