package dev.aikido.agent_api.vulnerabilities.ssrf.imds;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class TrustedHosts {
    private TrustedHosts() {}
    private static final String[] trustedHosts = new String[] {
        "metadata.google.internal",
        "metadata.goog"
    };

    /** Checks if this hostname is trusted */
    public static boolean isTrustedHostname(String hostname) {
        String normalized = hostname.toLowerCase();
        if (normalized.endsWith(".")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return Arrays.asList(trustedHosts).contains(normalized);
    }
}
