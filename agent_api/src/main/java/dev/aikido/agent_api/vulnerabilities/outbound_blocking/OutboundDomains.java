package dev.aikido.agent_api.vulnerabilities.outbound_blocking;

import dev.aikido.agent_api.storage.service_configuration.Domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutboundDomains {
    private Map<String, Domain> domains = new HashMap<>();
    private boolean blockNewOutgoingRequests = false;

    public void update(List<Domain> newDomains, boolean blockNewOutgoingRequests) {
        if (newDomains != null) {
            this.domains = new HashMap<>();
            for (Domain domain : newDomains) {
                this.domains.putIfAbsent(domain.hostname(), domain);
            }
        }
        this.blockNewOutgoingRequests = blockNewOutgoingRequests;
    }

    public boolean shouldBlockOutgoingRequest(String hostname) {
        Domain matchingDomain = this.domains.get(hostname);

        if (this.blockNewOutgoingRequests) {
            // Only allow outgoing requests if the mode is "allow"
            // null means unknown hostname, so they get blocked
            return matchingDomain == null || matchingDomain.isBlockingMode();
        }

        // Only block outgoing requests if the mode is "block"
        return matchingDomain != null && matchingDomain.isBlockingMode();
    }
}
