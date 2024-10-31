package dev.aikido.agent_api.vulnerabilities.ssrf.imds;

import java.util.HashSet;
import java.util.Set;

public class TrustedHosts {
    private static final Set<String> trustedHosts = new HashSet<>();

    static {
        trustedHosts.add("metadata.google.internal");
        trustedHosts.add("metadata.goog");
    }

    /** Checks if this hostname is trusted */
    public static boolean isTrustedHostname(String hostname) {
        return trustedHosts.contains(hostname);
    }
}
