package vulnerabilities.ssrf;

import org.junit.jupiter.api.Test;

import static dev.aikido.agent_api.vulnerabilities.ssrf.IsPrivateIP.isPrivateIp;
import static dev.aikido.agent_api.vulnerabilities.ssrf.IsPrivateIP.mapIPv4ToIPv6;
import static org.junit.jupiter.api.Assertions.*;

public class IsPrivateIPTest {

    @Test
    public void testMapIPv4ToIPv6() {
        assertEquals("::ffff:127.0.0.0/128", mapIPv4ToIPv6("127.0.0.0"));
        assertEquals("::ffff:127.0.0.0/104", mapIPv4ToIPv6("127.0.0.0/8"));
        assertEquals("::ffff:10.0.0.0/128", mapIPv4ToIPv6("10.0.0.0"));
        assertEquals("::ffff:10.0.0.0/104", mapIPv4ToIPv6("10.0.0.0/8"));
        assertEquals("::ffff:10.0.0.1/128", mapIPv4ToIPv6("10.0.0.1"));
        assertEquals("::ffff:10.0.0.1/104", mapIPv4ToIPv6("10.0.0.1/8"));
        assertEquals("::ffff:192.168.0.0/112", mapIPv4ToIPv6("192.168.0.0/16"));
        assertEquals("::ffff:172.16.0.0/108", mapIPv4ToIPv6("172.16.0.0/12"));
    }

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
