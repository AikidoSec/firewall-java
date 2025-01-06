package helpers;

import dev.aikido.agent_api.helpers.net.IPAddressRegex;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IPAddressRegexTest {

    // Test cases for IPv4 addresses
    @Test
    void testValidIPv4Addresses() {
        assertTrue(IPAddressRegex.isIPv4Address("192.168.1.1"));
        assertTrue(IPAddressRegex.isIPv4Address("255.255.255.255"));
        assertTrue(IPAddressRegex.isIPv4Address("0.0.0.0"));
        assertTrue(IPAddressRegex.isIPv4Address("10.0.0.1"));
        assertTrue(IPAddressRegex.isIPv4Address("172.16.254.1"));
    }

    @Test
    void testInvalidIPv4Addresses() {
        assertFalse(IPAddressRegex.isIPv4Address("256.256.256.256"));
        assertFalse(IPAddressRegex.isIPv4Address("192.168.1"));
        assertFalse(IPAddressRegex.isIPv4Address("192.168.1.1.1"));
        assertFalse(IPAddressRegex.isIPv4Address("192.168.1.-1"));
        assertFalse(IPAddressRegex.isIPv4Address("abc.def.ghi.jkl"));
    }

    // Test cases for standard IPv6 addresses
    @Test
    void testValidIPv6StdAddresses() {
        assertTrue(IPAddressRegex.isIPAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
        assertTrue(IPAddressRegex.isIPAddress("::1"));
        assertTrue(IPAddressRegex.isIPAddress("fe80::1ff:fe23:4567:890a"));
        assertTrue(IPAddressRegex.isIPAddress("::"));
    }

    @Test
    void testInvalidIPv6StdAddresses() {
        assertFalse(IPAddressRegex.isIPv6StdAddress("2001:db8:85a3:0:0:8a2e:370:7334:1234"));
        assertFalse(IPAddressRegex.isIPv6StdAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334:1234"));
        assertFalse(IPAddressRegex.isIPv6StdAddress("2001:db8:85a3::8a2e:370:7334:1234"));
        assertFalse(IPAddressRegex.isIPv6StdAddress("2001:db8:85a3:0000:0000:8a2e:0370:7334:1234"));
        assertFalse(IPAddressRegex.isIPv6StdAddress("2001:db8:85a3:0000:0000:8a2e:0370:7334:1234"));
    }

    // Test cases for compressed IPv6 addresses
    @Test
    void testValidIPv6HexCompressedAddresses() {
        assertTrue(IPAddressRegex.isIPv6HexCompressedAddress("2001:db8::1"));
        assertTrue(IPAddressRegex.isIPv6HexCompressedAddress("::1"));
        assertTrue(IPAddressRegex.isIPv6HexCompressedAddress("::"));
        assertTrue(IPAddressRegex.isIPv6HexCompressedAddress("2001:0db8:85a3::8a2e:0370:7334"));
    }

    // Test cases for general IP address validation
    @Test
    void testIsIPAddress() {
        assertTrue(IPAddressRegex.isIPAddress("192.168.1.1"));
        assertTrue(IPAddressRegex.isIPAddress("255.255.255.255"));
        assertTrue(IPAddressRegex.isIPAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
        assertTrue(IPAddressRegex.isIPAddress("::1"));
        assertTrue(IPAddressRegex.isIPAddress("2001:db8::1"));
        assertFalse(IPAddressRegex.isIPAddress("256.256.256.256"));
        assertFalse(IPAddressRegex.isIPAddress("192.168.1"));
        assertFalse(IPAddressRegex.isIPAddress("192.168.1.1.1"));
        assertFalse(IPAddressRegex.isIPAddress("abc.def.ghi.jkl"));
        assertTrue(IPAddressRegex.isIPAddress("2001:db8:85a3::8a2e:370:7334:1234"));
        assertFalse(IPAddressRegex.isIPAddress("2001:db8:::1"));
        assertFalse(IPAddressRegex.isIPAddress("2001:db8:85a3:0000:0000:8a2e:0370:7334:1234"));
    }
}