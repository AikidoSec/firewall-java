package dev.aikido.agent_api.vulnerabilities.ssrf;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.vulnerabilities.Attack;
import dev.aikido.agent_api.vulnerabilities.Vulnerabilities;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static dev.aikido.agent_api.helpers.StackTrace.getCurrentStackTrace;
import static dev.aikido.agent_api.vulnerabilities.ssrf.imds.Resolver.resolvesToImdsIp;

public class StoredSSRFDetector {
    public Attack run(String hostname, List<String> ipAddresses, String operation) {
        if(hostname == null || hostname.isEmpty()) {
            return null;
        }

        String imdsIp = resolvesToImdsIp(new HashSet<>(ipAddresses), hostname);
        if (imdsIp == null) {
            return null;
        }

        return new Attack(
            operation,
            new Vulnerabilities.StoredSSRFVulnerability(),
            null, // source is null for stored attacks
            "", // path is empty
            Map.of(
                "hostname", hostname,
                "privateIP", imdsIp
            ),
            hostname, // payload is the hostname
            getCurrentStackTrace(),
            null // user is null for stored attacks
        );
    }
}
