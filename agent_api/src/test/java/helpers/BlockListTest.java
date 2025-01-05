package helpers;

import dev.aikido.agent_api.helpers.net.BlockList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BlockListTest {
    private BlockList blocklist;

    @BeforeEach
    public void setUp() {
        blocklist = new BlockList();
    }

    @Test
    public void testBlocklistAddAddress() {
        assertFalse(blocklist.isBlocked("192.168.1.1"));
        blocklist.addAddress("192.168.1.1");
        assertTrue(blocklist.isBlocked("192.168.1.1"));

        // Test adding the same address again
        blocklist.addAddress("192.168.1.1");
        assertTrue(blocklist.isBlocked("192.168.1.1"));
    }

    @Test
    public void testBlocklistAddSubnet() {
        assertFalse(blocklist.isBlocked("10.0.0.1"));
        blocklist.addSubnet("10.0.0.0", 8);
        assertTrue(blocklist.isBlocked("10.0.0.1"));
        assertTrue(blocklist.isBlocked("10.1.1.1"));
        assertFalse(blocklist.isBlocked("192.168.1.1"));
    }

    @Test
    public void testBlocklistMultipleAddressesAndSubnets() {
        blocklist.addAddress("192.168.1.1");
        blocklist.addSubnet("10.0.0.0", 8);

        assertTrue(blocklist.isBlocked("192.168.1.1"));
        assertTrue(blocklist.isBlocked("10.0.0.1"));
        assertTrue(blocklist.isBlocked("10.1.1.1"));
        assertFalse(blocklist.isBlocked("172.16.0.1"));
    }

    @Test
    public void testBlocklistInvalidIp() {
        blocklist.addAddress("192.168.1.1");
        assertFalse(blocklist.isBlocked("invalid_ip"));
    }

    @Test
    public void testBlocklistSubnetEdgeCases() {
        blocklist.addSubnet("192.168.1.0", 24);

        assertTrue(blocklist.isBlocked("192.168.1.255")); // Last address in the subnet
        assertTrue(blocklist.isBlocked("192.168.1.0")); // First address in the subnet
        assertFalse(blocklist.isBlocked("192.168.2.1")); // Outside the subnet
        assertTrue(blocklist.isBlocked("192.168.1.128")); // Middle of the subnet
    }

    @Test
    public void testBlocklistIpv6() {
        blocklist.addAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334");
        assertTrue(blocklist.isBlocked("2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
        assertFalse(blocklist.isBlocked("::1")); // Not in the blocklist

        blocklist.addSubnet("2001:0db8::", 32);
        assertTrue(blocklist.isBlocked("2001:0db8:abcd:0012:0000:0000:0000:0001"));
        assertFalse(blocklist.isBlocked("2001:0db9::"));
    }

    @Test
    public void testBlocklistOverlappingSubnets() {
        blocklist.addSubnet("192.168.1.0", 24); // Covers 192.168.1.0 to 192.168.1.255
        blocklist.addSubnet("192.168.1.128", 25); // Covers 192.168.1.128 to 192.168.1.255

        assertTrue(blocklist.isBlocked("192.168.1.130")); // Inside both subnets
        assertTrue(blocklist.isBlocked("192.168.1.0")); // Inside first subnet
        assertTrue(blocklist.isBlocked("192.168.1.127")); // Inside first subnet, outside second
        assertTrue(blocklist.isBlocked("192.168.1.255")); // Last address in both subnets
        assertFalse(blocklist.isBlocked("192.168.2.1")); // Outside both subnets
    }

    @Test
    public void testBlocklistMixedAddressTypes() {
        blocklist.addAddress("192.168.1.1");
        blocklist.addAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334");

        assertTrue(blocklist.isBlocked("192.168.1.1"));
        assertTrue(blocklist.isBlocked("2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
        assertFalse(blocklist.isBlocked("192.168.1.2")); // Different IPv4
        assertFalse(blocklist.isBlocked("2001:0db8:85a3:0000:0000:8a2e:0370:7335")); // Different IPv6
    }

    @Test
    public void testBlocklistSubnetWithSingleIp() {
        blocklist.addSubnet("192.168.1.1", 32); // Single IP subnet

        assertTrue(blocklist.isBlocked("192.168.1.1"));
        assertFalse(blocklist.isBlocked("192.168.1.2")); // Outside the subnet
    }
}
