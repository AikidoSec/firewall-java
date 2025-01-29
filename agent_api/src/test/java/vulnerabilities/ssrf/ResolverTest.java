package vulnerabilities.ssrf;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.aikido.agent_api.vulnerabilities.ssrf.imds.Resolver;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class ResolverTest {
    @Test
    void testResolvesToImdsIp_WithTrustedHostname() {
        Set<String> resolvedIps = new HashSet<>();
        resolvedIps.add("192.168.1.1"); // Non-IMDS IP

        assertFalse(Resolver.resolvesToImdsIp(resolvedIps, "metadata.google.internal"));
    }

    @Test
    void testResolvesToImdsIp_WithImdsIp() {
        Set<String> resolvedIps = new HashSet<>();
        resolvedIps.add("169.254.169.254"); // IMDS IP

        assertTrue(Resolver.resolvesToImdsIp(resolvedIps, "example.com"));
    }

    @Test
    void testResolvesToImdsIp_WithMultipleResolvedIps_OneImdsIp() {
        Set<String> resolvedIps = new HashSet<>();
        resolvedIps.add("192.168.1.1"); // Non-IMDS IP
        resolvedIps.add("fd00:ec2::254"); // IMDS IP

        assertTrue(Resolver.resolvesToImdsIp(resolvedIps, "example.com"));
    }

    @Test
    void testResolvesToImdsIp_WithNoImdsIp() {
        Set<String> resolvedIps = new HashSet<>();
        resolvedIps.add("192.168.1.1"); // Non-IMDS IP
        resolvedIps.add("10.0.0.1"); // Another Non-IMDS IP

        assertFalse(Resolver.resolvesToImdsIp(resolvedIps, "example.com"));
    }

    @Test
    void testResolvesToImdsIp_WithMultipleResolvedIps_NoImdsIp() {
        Set<String> resolvedIps = new HashSet<>();
        resolvedIps.add("192.168.1.1"); // Non-IMDS IP
        resolvedIps.add("10.0.0.1"); // Another Non-IMDS IP

        assertFalse(Resolver.resolvesToImdsIp(resolvedIps, "example.com"));
    }

    @Test
    void testResolvesToImdsIp_WithMultipleResolvedIps_OnlyTrustedHostname() {
        Set<String> resolvedIps = new HashSet<>();
        resolvedIps.add("169.254.169.254"); // IMDS IP

        assertFalse(Resolver.resolvesToImdsIp(resolvedIps, "metadata.google.internal"));
    }
}
