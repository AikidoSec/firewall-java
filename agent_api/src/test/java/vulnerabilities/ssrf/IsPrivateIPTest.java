package vulnerabilities.ssrf;

import org.junit.jupiter.api.Test;

import static dev.aikido.agent_api.vulnerabilities.ssrf.IsPrivateIP.isPrivateIp;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IsPrivateIPTest {

    @Test
    void testPrivateIPv4Addresses() {
        assertTrue(isPrivateIp("192.168.1.1"));
        assertTrue(isPrivateIp("192.168.1.10"));
        assertTrue(isPrivateIp("10.0.0.1"));
        assertTrue(isPrivateIp("10.0.0.8"));
        assertTrue(isPrivateIp("172.16.0.1"));
        assertTrue(isPrivateIp("127.0.0.1"));
        assertTrue(isPrivateIp("169.254.1.1")); // Note: 169.254.x.x is link-local, not private
    }

    @Test
    void testPublicIPv4Addresses() {
        assertFalse(isPrivateIp("8.8.8.8"));
        assertFalse(isPrivateIp("172.15.0.1"));
    }

    @Test
    void testPrivateIPv6Addresses() {
        assertTrue(isPrivateIp("::1")); // Loopback address
        assertTrue(isPrivateIp("fc00::1")); // Unique local address
        assertTrue(isPrivateIp("fe80::1")); // Link-local address
    }

    @Test
    void testPublicIPv6Addresses() {
        assertFalse(isPrivateIp("2001:db8::1")); // Documentation address
        assertFalse(isPrivateIp("::ffff:8.8.8.8")); // IPv4-mapped address
    }

    @Test
    void testInvalidAddresses() {
        assertFalse(isPrivateIp("invalid-ip"));
        assertFalse(isPrivateIp("256.256.256.256")); // Invalid IPv4
        assertFalse(isPrivateIp("::g")); // Invalid IPv6
    }

}
