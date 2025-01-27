package vulnerabilities.ssrf;


import dev.aikido.agent_api.vulnerabilities.ssrf.imds.BlockList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IPListTest {

    private BlockList blockList;

    @BeforeEach
    void setUp() {
        blockList = new BlockList();
    }

    @Test
    void testAddIPv4Address() {
        blockList.addAddress("192.168.1.1", "ipv4");
        assertTrue(blockList.check("192.168.1.1", "ipv4"), "IPv4 address should be blocked");
        assertTrue(blockList.check("192.168.1.1", "unknown"), "IPv4 address should be blocked");
    }

    @Test
    void testAddIPv6Address() {
        blockList.addAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334", "ipv6");
        assertTrue(blockList.check("2001:0db8:85a3:0000:0000:8a2e:0370:7334", "ipv6"), "IPv6 address should be blocked");
        assertTrue(blockList.check("2001:0db8:85a3:0000:0000:8a2e:0370:7334", "unknown"), "IPv6 address should be blocked");

    }

    @Test
    void testAddIPv8AddressDoesNotWork() {
        blockList.addAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334", "ipv8");
        assertFalse(blockList.check("2001:0db8:85a3:0000:0000:8a2e:0370:7334", "ipv4"));
        assertFalse(blockList.check("2001:0db8:85a3:0000:0000:8a2e:0370:7334", "ipv6"));
        assertFalse(blockList.check("2001:0db8:85a3:0000:0000:8a2e:0370:7334", "ipv8"));
    }

    @Test
    void testCheckBlockedIPv4Address() {
        blockList.addAddress("10.0.0.1", "ipv4");
        assertTrue(blockList.check("10.0.0.1", "ipv4"), "IPv4 address should be blocked");
    }

    @Test
    void testCheckBlockedIPv6Address() {
        blockList.addAddress("::1", "ipv6");
        assertTrue(blockList.check("::1", "ipv6"), "IPv6 address should be blocked");
    }

    @Test
    void testCheckUnblockedIPv4Address() {
        blockList.addAddress("192.168.1.1", "ipv4");
        assertFalse(blockList.check("192.168.1.2", "ipv4"), "IPv4 address should not be blocked");
    }

    @Test
    void testCheckUnblockedIPv6Address() {
        blockList.addAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334", "ipv6");
        assertFalse(blockList.check("2001:0db8:85a3:0000:0000:8a2e:0370:7335", "ipv6"), "IPv6 address should not be blocked");
    }

    @Test
    void testCheckNullAddressType() {
        blockList.addAddress("192.168.1.1", "ipv4");
        assertTrue(blockList.check("192.168.1.1", null), "IPv4 address should be blocked when checking with null type");
    }

    @Test
    void testCheckEmptyAddress() {
        assertFalse(blockList.check("", "ipv4"), "Empty address should not be blocked");
        assertFalse(blockList.check("", "ipv6"), "Empty address should not be blocked");
    }

    @Test
    void testAddDuplicateIPv4Address() {
        blockList.addAddress("192.168.1.1", "ipv4");
        blockList.addAddress("192.168.1.1", "ipv4"); // Adding duplicate
        assertTrue(blockList.check("192.168.1.1", "ipv4"), "IPv4 address should still be blocked");
    }

    @Test
    void testAddDuplicateIPv6Address() {
        blockList.addAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334", "ipv6");
        blockList.addAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334", "ipv6"); // Adding duplicate
        assertTrue(blockList.check("2001:0db8:85a3:0000:0000:8a2e:0370:7334", "ipv6"), "IPv6 address should still be blocked");
    }

    @Test
    void testCheckBlockedIPv4AndIPv6() {
        blockList.addAddress("192.168.1.1", "ipv4");
        blockList.addAddress("::1", "ipv6");
        assertTrue(blockList.check("192.168.1.1", "ipv4"), "IPv4 address should be blocked");
        assertTrue(blockList.check("::1", "ipv6"), "IPv6 address should be blocked");
    }
}