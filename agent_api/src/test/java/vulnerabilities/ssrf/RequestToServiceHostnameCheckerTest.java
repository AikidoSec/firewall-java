package vulnerabilities.ssrf;

import dev.aikido.agent_api.vulnerabilities.ssrf.RequestToServiceHostnameChecker;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RequestToServiceHostnameCheckerTest {

    @Test
    void testValidHostnames() {
        assertTrue(RequestToServiceHostnameChecker.isRequestToServiceHostname("valid_hostname"));
        assertTrue(RequestToServiceHostnameChecker.isRequestToServiceHostname("valid-hostname"));
        assertTrue(RequestToServiceHostnameChecker.isRequestToServiceHostname("valid123"));
        assertTrue(RequestToServiceHostnameChecker.isRequestToServiceHostname("hostname_with_underscores-and-dashes"));
        assertTrue(RequestToServiceHostnameChecker.isRequestToServiceHostname("123456"));
        assertTrue(RequestToServiceHostnameChecker.isRequestToServiceHostname("a-b_c"));
    }

    @Test
    void testInvalidHostnames() {
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname(null));
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname(""));
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname(" "));
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("invalid@hostname"));
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("invalid#hostname"));
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("invalid/hostname"));
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("invalid:hostname"));
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("invalid;hostname"));
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("invalid.hostname"));
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("invalid_hostname!"));
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("invalid-hostname*"));
    }

    @Test
    void testEdgeCases() {
        assertTrue(RequestToServiceHostnameChecker.isRequestToServiceHostname("-leadingdash"));
        assertTrue(RequestToServiceHostnameChecker.isRequestToServiceHostname("_leadingunderscore"));
        assertTrue(RequestToServiceHostnameChecker.isRequestToServiceHostname("trailingdash-"));
        assertTrue(RequestToServiceHostnameChecker.isRequestToServiceHostname("trailingunderscore_"));
        assertTrue(RequestToServiceHostnameChecker.isRequestToServiceHostname("dash--dash"));
        assertTrue(RequestToServiceHostnameChecker.isRequestToServiceHostname("underscore__underscore"));

        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("-leadingdash."));
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("_leadingunderscore."));
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname(".trailingdash-"));
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname(".trailingunderscore_"));
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("dash--dash."));
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname(".underscore__underscore"));
    }

    @Test
    void testMixedValidAndInvalidCharacters() {
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("valid_hostname!@#"));
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("valid-hostname$%^"));
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("valid123&*()"));
    }

    @Test
    void testAllowedLocalhostVariants() {
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("localhost"));
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("localhost.localdomain"));
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("LOCALHOST"));
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("LocalHost"));
    }

    @Test
    void testOtherLocalhostVariants() {
        // If you want to test other variants that are not in the allowed list
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("127.0.0.1"));
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("::1"));
    }

    @Test
    void testAllowedIPv4Addresses() {
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("192.168.1.1"));
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("255.255.255.255"));
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("0.0.0.0"));
    }

    @Test
    void testAllowedIPv6Addresses() {
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("::1"));
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("::ffff:192.168.1.1"));
    }

    @Test
    void testAllowedNormalHostnames() {
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("google.com"));
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("subdomain.example.com"));
        assertFalse(RequestToServiceHostnameChecker.isRequestToServiceHostname("example.com"));
    }
}
