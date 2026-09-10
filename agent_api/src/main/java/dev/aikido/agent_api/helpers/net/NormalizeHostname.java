package dev.aikido.agent_api.helpers.net;

import java.net.IDN;

public final class NormalizeHostname {
    private NormalizeHostname() {}

    /**
     * Canonicalizes a hostname for consistent comparison: lowercases, strips a
     * trailing dot (FQDN form returned by some DNS resolvers), and converts
     * Punycode labels to Unicode via IDN.
     */
    public static String normalize(String hostname) {
        if (hostname == null || hostname.isEmpty()) {
            return hostname;
        }
        String lower = hostname.toLowerCase();
        String noDot = lower.endsWith(".") ? lower.substring(0, lower.length() - 1) : lower;
        try {
            return IDN.toUnicode(noDot);
        } catch (Exception e) {
            return noDot;
        }
    }
}
