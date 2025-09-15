package dev.aikido.agent_api.vulnerabilities.ssrf.imds;

import java.util.Set;

public final class Resolver {
    private Resolver() {}
    /**
     * Returns the IMDS IP if found, otherwise null
     */
    public static String resolvesToImdsIp(Set<String> resolvedIpAddresses, String hostname) {
        // Allow access to Google Cloud metadata service as you need to set specific headers to access it
        // We don't want to block legitimate requests
        if (TrustedHosts.isTrustedHostname(hostname)) {
            return null;
        }
        for (String ip : resolvedIpAddresses) {
            if (IMDSAddresses.isImdsIpAddress(ip)) {
                return ip;
            }
        }
        return null;
    }
}
