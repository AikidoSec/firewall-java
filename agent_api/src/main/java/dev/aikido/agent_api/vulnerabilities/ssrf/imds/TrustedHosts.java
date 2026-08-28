package dev.aikido.agent_api.vulnerabilities.ssrf.imds;

import dev.aikido.agent_api.helpers.net.NormalizeHostname;
import java.util.Arrays;

public final class TrustedHosts {
    private TrustedHosts() {}
    private static final String[] trustedHosts = new String[] {
        "metadata.google.internal",
        "metadata.goog"
    };

    /** Checks if this hostname is trusted */
    public static boolean isTrustedHostname(String hostname) {
        return Arrays.asList(trustedHosts).contains(NormalizeHostname.normalize(hostname));
    }
}
