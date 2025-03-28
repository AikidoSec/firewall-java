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
        assertTrue(isPrivateIp("0.0.0.0"));
        assertTrue(isPrivateIp("0000.0000"));
        assertTrue(isPrivateIp("0.0.0.1"));
        assertTrue(isPrivateIp("0.0.0.7"));
        assertTrue(isPrivateIp("0.0.0.255"));
        assertTrue(isPrivateIp("0.0.255.255"));
        assertTrue(isPrivateIp("0.1.255.255"));
        assertTrue(isPrivateIp("0.15.255.255"));
        assertTrue(isPrivateIp("0.63.255.255"));
        assertTrue(isPrivateIp("0.255.255.254"));
        assertTrue(isPrivateIp("0.255.255.255"));
        assertTrue(isPrivateIp("10.0.0.0"));
        assertTrue(isPrivateIp("10.0.0.1"));
        assertTrue(isPrivateIp("10.0.0.01")); // This is equivalent to 10.0.0.1
        assertTrue(isPrivateIp("10.0.0.001")); // This is equivalent to 10.0.0.1
        assertTrue(isPrivateIp("10.255.255.254"));
        assertTrue(isPrivateIp("10.255.255.255"));
        assertTrue(isPrivateIp("100.64.0.0"));
        assertTrue(isPrivateIp("100.64.0.1"));
        assertTrue(isPrivateIp("100.127.255.254"));
        assertTrue(isPrivateIp("100.127.255.255"));
        assertTrue(isPrivateIp("127.0.0.0"));
        assertTrue(isPrivateIp("127.0.0.1"));
        assertTrue(isPrivateIp("127.0.0.01")); // This is equivalent to 127.0.0.1
        assertTrue(isPrivateIp("127.1")); // This is equivalent to 127.0.0.1
        assertTrue(isPrivateIp("127.0.1")); // This is equivalent to 127.0.0.1
        assertTrue(isPrivateIp("127.000.000.1")); // This is equivalent to 127.0.0.1
        assertTrue(isPrivateIp("127.255.255.254"));
        assertTrue(isPrivateIp("127.255.255.255"));
        assertTrue(isPrivateIp("169.254.0.0"));
        assertTrue(isPrivateIp("169.254.0.1"));
        assertTrue(isPrivateIp("169.254.255.254"));
        assertTrue(isPrivateIp("169.254.255.255"));
        assertTrue(isPrivateIp("172.16.0.0"));
        assertTrue(isPrivateIp("172.16.0.1"));
        assertTrue(isPrivateIp("172.16.0.001")); // This is equivalent to 172.16.0.1
        assertTrue(isPrivateIp("172.31.255.254"));
        assertTrue(isPrivateIp("172.31.255.255"));
        assertTrue(isPrivateIp("192.0.0.0"));
        assertTrue(isPrivateIp("192.0.0.1"));
        assertTrue(isPrivateIp("192.0.0.6"));
        assertTrue(isPrivateIp("192.0.0.7"));
        assertTrue(isPrivateIp("192.0.0.8"));
        assertTrue(isPrivateIp("192.0.0.9"));
        assertTrue(isPrivateIp("192.0.0.10"));
        assertTrue(isPrivateIp("192.0.0.11"));
        assertTrue(isPrivateIp("192.0.0.170"));
        assertTrue(isPrivateIp("192.0.0.171"));
        assertTrue(isPrivateIp("192.0.0.254"));
        assertTrue(isPrivateIp("192.0.0.255"));
        assertTrue(isPrivateIp("192.0.2.0"));
        assertTrue(isPrivateIp("192.0.2.1"));
        assertTrue(isPrivateIp("192.0.2.254"));
        assertTrue(isPrivateIp("192.0.2.255"));
        assertTrue(isPrivateIp("192.31.196.0"));
        assertTrue(isPrivateIp("192.31.196.1"));
        assertTrue(isPrivateIp("192.31.196.254"));
        assertTrue(isPrivateIp("192.31.196.255"));
        assertTrue(isPrivateIp("192.52.193.0"));
        assertTrue(isPrivateIp("192.52.193.1"));
        assertTrue(isPrivateIp("192.52.193.254"));
        assertTrue(isPrivateIp("192.52.193.255"));
        assertTrue(isPrivateIp("192.88.99.0"));
        assertTrue(isPrivateIp("192.88.99.1"));
        assertTrue(isPrivateIp("192.88.99.254"));
        assertTrue(isPrivateIp("192.88.99.255"));
        assertTrue(isPrivateIp("192.168.0.0"));
        assertTrue(isPrivateIp("192.168.0.1"));
        assertTrue(isPrivateIp("192.168.255.254"));
        assertTrue(isPrivateIp("192.168.255.255"));
        assertTrue(isPrivateIp("192.175.48.0"));
        assertTrue(isPrivateIp("192.175.48.1"));
        assertTrue(isPrivateIp("192.175.48.254"));
        assertTrue(isPrivateIp("192.175.48.255"));
        assertTrue(isPrivateIp("198.18.0.0"));
        assertTrue(isPrivateIp("198.18.0.1"));
        assertTrue(isPrivateIp("198.19.255.254"));
        assertTrue(isPrivateIp("198.19.255.255"));
        assertTrue(isPrivateIp("198.51.100.0"));
        assertTrue(isPrivateIp("198.51.100.1"));
        assertTrue(isPrivateIp("198.51.100.254"));
        assertTrue(isPrivateIp("198.51.100.255"));
        assertTrue(isPrivateIp("203.0.113.0"));
        assertTrue(isPrivateIp("203.0.113.1"));
        assertTrue(isPrivateIp("203.0.113.254"));
        assertTrue(isPrivateIp("203.0.113.255"));
        assertTrue(isPrivateIp("240.0.0.0"));
        assertTrue(isPrivateIp("240.0.0.1"));
        assertTrue(isPrivateIp("224.0.0.0"));
        assertTrue(isPrivateIp("224.0.0.1"));
        assertTrue(isPrivateIp("255.0.0.0"));
        assertTrue(isPrivateIp("255.192.0.0"));
        assertTrue(isPrivateIp("255.240.0.0"));
        assertTrue(isPrivateIp("255.254.0.0"));
        assertTrue(isPrivateIp("255.255.0.0"));
        assertTrue(isPrivateIp("255.255.255.0"));
        assertTrue(isPrivateIp("255.255.255.248"));
        assertTrue(isPrivateIp("255.255.255.254"));
        assertTrue(isPrivateIp("255.255.255.255"));
    }


    @Test
    void testPublicIPv4Addresses() {
        assertFalse(isPrivateIp("44.37.112.180"));
        assertFalse(isPrivateIp("46.192.247.73"));
        assertFalse(isPrivateIp("71.12.102.112"));
        assertFalse(isPrivateIp("101.0.26.90"));
        assertFalse(isPrivateIp("111.211.73.40"));
        assertFalse(isPrivateIp("156.238.194.84"));
        assertFalse(isPrivateIp("164.101.185.82"));
        assertFalse(isPrivateIp("223.231.138.242"));
    }

    @Test
    void testPrivateIPv6Addresses() {
        assertTrue(isPrivateIp("0000:0000:0000:0000:0000:0000:0000:0000"));
        assertTrue(isPrivateIp("::"));
        assertTrue(isPrivateIp("::1"));
        assertTrue(isPrivateIp("::ffff:0.0.0.0"));
        assertTrue(isPrivateIp("::ffff:127.0.0.1"));
        assertTrue(isPrivateIp("::ffff:127.0.0.2"));
        assertTrue(isPrivateIp("::ffff:10.0.0.1"));
        assertTrue(isPrivateIp("::ffff:172.16.1.2"));
        assertTrue(isPrivateIp("::ffff:192.168.2.2"));
        assertTrue(isPrivateIp("fe80::"));
        assertTrue(isPrivateIp("fe80::1"));
        assertTrue(isPrivateIp("fe80::abc:1"));
        assertTrue(isPrivateIp("febf:ffff:ffff:ffff:ffff:ffff:ffff:ffff"));
        assertTrue(isPrivateIp("fc00::"));
        assertTrue(isPrivateIp("fc00::1"));
        assertTrue(isPrivateIp("fc00::abc:1"));
        assertTrue(isPrivateIp("fdff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"));
        assertTrue(isPrivateIp("fd00:ec2::254"));
    }

    @Test
    void testPublicIPv6Addresses() {
        assertFalse(isPrivateIp("::1fff:0.0.0.0"));
        assertFalse(isPrivateIp("::1fff:10.0.0.0"));
        assertFalse(isPrivateIp("::1fff:0:0.0.0.0"));
        assertFalse(isPrivateIp("::1fff:0:10.0.0.0"));
        assertFalse(isPrivateIp("2001:2:ffff:ffff:ffff:ffff:ffff:ffff"));
        assertFalse(isPrivateIp("64:ff9a::0.0.0.0"));
        assertFalse(isPrivateIp("64:ff9a::255.255.255.255"));
        assertFalse(isPrivateIp("99::"));
        assertFalse(isPrivateIp("99::ffff:ffff:ffff:ffff"));
        assertFalse(isPrivateIp("101::"));
        assertFalse(isPrivateIp("101::ffff:ffff:ffff:ffff"));
        assertFalse(isPrivateIp("2000::"));
        assertFalse(isPrivateIp("2000::ffff:ffff:ffff:ffff:ffff:ffff"));
        assertFalse(isPrivateIp("2001:10::"));
        assertFalse(isPrivateIp("2001:1f:ffff:ffff:ffff:ffff:ffff:ffff"));
        assertFalse(isPrivateIp("2001:db7::"));
        assertFalse(isPrivateIp("2001:db7:ffff:ffff:ffff:ffff:ffff:ffff"));
        assertFalse(isPrivateIp("2001:db9::"));
        assertFalse(isPrivateIp("fb00::"));
        assertFalse(isPrivateIp("fbff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"));
        assertFalse(isPrivateIp("fec0::"));
        assertFalse(isPrivateIp("::ffff:1.2.3.4")); // IPv4-mapped address
        assertFalse(isPrivateIp("::ffff:172.1.2.3")); // IPv4-mapped address
        assertFalse(isPrivateIp("::ffff:192.145.0.0")); // IPv4-mapped address
    }


    @Test
    void testInvalidAddresses() {
        assertFalse(isPrivateIp("invalid-ip"));
        assertFalse(isPrivateIp("256.256.256.256")); // Invalid IPv4
        assertFalse(isPrivateIp("::g")); // Invalid IPv6
    }

}
