package dev.aikido.agent_api.vulnerabilities.outbound_blocking;

import dev.aikido.agent_api.storage.service_configuration.Domain;

import java.net.IDN;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OutboundDomains {
    private Map<String, String> domains = new HashMap<>();
    private boolean blockNewOutgoingRequests = false;

    public void update(List<Domain> newDomains, boolean blockNewOutgoingRequests) {
        if (newDomains != null) {
            this.domains = new HashMap<>();
            for (Domain domain : newDomains) {
                this.domains.put(normalize(domain.hostname()), domain.mode());
            }
        }
        this.blockNewOutgoingRequests = blockNewOutgoingRequests;
    }

    public boolean shouldBlockOutgoingRequest(String hostname) {
        String mode = this.domains.get(normalize(hostname));

        if (this.blockNewOutgoingRequests) {
            // Only allow outgoing requests if the mode is "allow"
            // null means unknown hostname, so they get blocked
            return !"allow".equals(mode);
        }

        // Only block outgoing requests if the mode is "block"
        return "block".equals(mode);
    }

    // Normalize to lowercased Unicode form so Punycode (xn--...) and Unicode
    // variants of the same hostname compare equal.
    private static String normalize(String hostname) {
        if (hostname == null) {
            return null;
        }
        String lower = hostname.toLowerCase(Locale.ROOT);
        try {
            return IDN.toUnicode(lower, IDN.ALLOW_UNASSIGNED);
        } catch (IllegalArgumentException e) {
            return lower;
        }
    }
}
