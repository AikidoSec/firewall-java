package dev.aikido.agent_api.vulnerabilities.ssrf;

import dev.aikido.agent_api.vulnerabilities.Detector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.aikido.agent_api.helpers.ShouldBlockHelper.shouldBlock;
import static dev.aikido.agent_api.vulnerabilities.ssrf.IsPrivateIP.containsPrivateIP;
import static dev.aikido.agent_api.vulnerabilities.ssrf.imds.Resolver.resolvesToImdsIp;

public class SSRFDetector {
    public Detector.DetectorResult run(String hostname, int port, List<String> ipAddresses) {
        if (resolvesToImdsIp(new HashSet<>(ipAddresses), hostname)) {
            // An attacker could have stored a hostname in a database that points to an IMDS IP address
            // We don't check if the user input contains the hostname because context might not be available
            if(shouldBlock()) {
                throw SSRFException.get();
            }
        }
        if (!containsPrivateIP(ipAddresses)) {
            // No real danger, returning.
            return null;
        }
        return null;
    }
}
