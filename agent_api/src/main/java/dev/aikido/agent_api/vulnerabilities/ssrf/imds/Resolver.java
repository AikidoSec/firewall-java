package dev.aikido.agent_api.vulnerabilities.ssrf.imds;

import java.util.Set;

public final class Resolver {
    private Resolver() {}
    /**
     * Returns boolean value that's true if this is an IMDS IP/Hostname
     */
    public static boolean resolvesToImdsIp(Set<String> resolvedIpAddresses, String hostname) {
        // Allow access to Google Cloud metadata service as you need to set specific headers to access it
        // We don't want to block legitimate requests
        if (TrustedHosts.isTrustedHostname(hostname)) {
            return false;
        }
        for (String ip : resolvedIpAddresses) {
            if (IMDSAddresses.isImdsIpAddress(ip)) {
                return true;
            }
        }
        return false;
    }
}