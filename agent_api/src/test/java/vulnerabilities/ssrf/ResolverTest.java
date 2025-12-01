package vulnerabilities.ssrf;

import dev.aikido.agent_api.vulnerabilities.ssrf.imds.Resolver;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static dev.aikido.agent_api.vulnerabilities.ssrf.imds.Resolver.resolvesToImdsIp;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ResolverTest {
    @Test
    void testResolvesToImdsIp_WithTrustedHostname() {
        Set<String> resolvedIps = new HashSet<>();
        resolvedIps.add("192.168.1.1"); // Non-IMDS IP

        assertNull(Resolver.resolvesToImdsIp(resolvedIps, "metadata.google.internal"));
    }

    @Test
    void testResolvesToImdsIp_WithImdsIp() {
        Set<String> resolvedIps = new HashSet<>();
        resolvedIps.add("169.254.169.254"); // IMDS IP

        assertEquals("169.254.169.254", Resolver.resolvesToImdsIp(resolvedIps, "example.com"));
    }

    @Test
    void testResolvesToImdsIp_WithIpv4MappedIP() {
        Set<String> resolvedIps = new HashSet<>();
        resolvedIps.add("::ffff:169.254.169.254"); // IMDS IP

        assertEquals("::ffff:169.254.169.254", Resolver.resolvesToImdsIp(resolvedIps, "example.com"));
    }

    @Test
    void testResolvesToImdsIp_WithIpv4MappedIP2() {
        Set<String> resolvedIps = new HashSet<>();
        resolvedIps.add("::ffff:100.100.100.200"); // IMDS IP

        assertEquals("::ffff:100.100.100.200", Resolver.resolvesToImdsIp(resolvedIps, "example.com"));
    }

    @Test
    void testDoesntResolveToImdsIp_WithHostnameImdsIp() {
        Set<String> resolvedIps = new HashSet<>();
        resolvedIps.add("169.254.169.254"); // IMDS IP

        assertNull(Resolver.resolvesToImdsIp(resolvedIps, " 169.254.169.254 "));
    }

    @Test
    void testResolvesToImdsIp_WithMultipleResolvedIps_OneImdsIp() {
        Set<String> resolvedIps = new HashSet<>();
        resolvedIps.add("192.168.1.1"); // Non-IMDS IP
        resolvedIps.add("fd00:ec2::254"); // IMDS IP

        assertEquals("fd00:ec2::254", Resolver.resolvesToImdsIp(resolvedIps, "example.com"));
    }

    @Test
    void testResolvesToImdsIp_WithNoImdsIp() {
        Set<String> resolvedIps = new HashSet<>();
        resolvedIps.add("192.168.1.1"); // Non-IMDS IP
        resolvedIps.add("10.0.0.1"); // Another Non-IMDS IP

        assertNull(Resolver.resolvesToImdsIp(resolvedIps, "example.com"));
    }

    @Test
    void testResolvesToImdsIp_WithMultipleResolvedIps_NoImdsIp() {
        Set<String> resolvedIps = new HashSet<>();
        resolvedIps.add("192.168.1.1"); // Non-IMDS IP
        resolvedIps.add("10.0.0.1"); // Another Non-IMDS IP

        assertNull(Resolver.resolvesToImdsIp(resolvedIps, "example.com"));
    }

    @Test
    void testResolvesToImdsIp_WithMultipleResolvedIps_OnlyTrustedHostname() {
        Set<String> resolvedIps = new HashSet<>();
        resolvedIps.add("169.254.169.254"); // IMDS IP

        assertNull(Resolver.resolvesToImdsIp(resolvedIps, "metadata.google.internal"));
    }
}
