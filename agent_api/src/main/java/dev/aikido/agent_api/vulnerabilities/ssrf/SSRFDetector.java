package dev.aikido.agent_api.vulnerabilities.ssrf;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.vulnerabilities.Attack;
import dev.aikido.agent_api.vulnerabilities.Detector;
import dev.aikido.agent_api.vulnerabilities.Vulnerabilities;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static dev.aikido.agent_api.helpers.ShouldBlockHelper.shouldBlock;
import static dev.aikido.agent_api.helpers.StackTrace.getCurrentStackTrace;
import static dev.aikido.agent_api.vulnerabilities.ssrf.FindHostnameInContext.findHostnameInContext;
import static dev.aikido.agent_api.vulnerabilities.ssrf.IsPrivateIP.containsPrivateIP;
import static dev.aikido.agent_api.vulnerabilities.ssrf.PrivateIPRedirectFinder.isRedirectToPrivateIP;
import static dev.aikido.agent_api.vulnerabilities.ssrf.imds.Resolver.resolvesToImdsIp;

public class SSRFDetector {
    public Attack run(String hostname, int port, List<String> ipAddresses, String operation) {
        if(hostname == null || hostname.isEmpty()) {
            return null;
        }

        if (!containsPrivateIP(ipAddresses)) {
            // No real danger, returning.
            return null;
        }

        ContextObject context = Context.get();
        if(context == null) {
            return null;
        }
        FindHostnameInContext.Res attackFindings = findHostnameInContext(hostname, context, port);
        if (attackFindings == null) {
            attackFindings = isRedirectToPrivateIP(hostname, port);
        }
        if(attackFindings != null) {
            return new Attack(
                    operation,
                    Vulnerabilities.SSRF,
                    attackFindings.source(),
                    attackFindings.pathToPayload(),
                    /*metadata*/ Map.of(
                        "hostname", hostname,
                        "port", String.valueOf(port)
                    ),
                    attackFindings.payload(),
                    getCurrentStackTrace(),
                    context.getUser()
            );
        }

        return null;
    }
}
