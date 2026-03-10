package dev.aikido.agent_api.vulnerabilities.outbound_blocking;

import dev.aikido.agent_api.storage.service_configuration.Domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutboundDomains {
    private Map<String, String> domains = new HashMap<>();
    private boolean blockNewOutgoingRequests = false;

    public void update(List<Domain> newDomains, boolean blockNewOutgoingRequests) {
        if (newDomains != null) {
            this.domains = new HashMap<>();
            for (Domain domain : newDomains) {
                this.domains.put(domain.hostname(), domain.mode());
            }
        }
        this.blockNewOutgoingRequests = blockNewOutgoingRequests;
    }

    public boolean shouldBlockOutgoingRequest(String hostname) {
        String mode = this.domains.get(hostname);

        if (this.blockNewOutgoingRequests) {
            // Only allow outgoing requests if the mode is "allow"
            // null means unknown hostname, so they get blocked
            return !"allow".equals(mode);
        }

        // Only block outgoing requests if the mode is "block"
        return "block".equals(mode);
    }
}
