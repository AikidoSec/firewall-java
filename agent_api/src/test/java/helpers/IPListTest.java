package helpers;

import dev.aikido.agent_api.helpers.net.IPList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IPListTest {
    private IPList blocklist;

    @BeforeEach
    public void setUp() {
        blocklist = new IPList();
    }

    @Test
    public void testBlocklistadd() {
        assertFalse(blocklist.matches("192.168.1.1"));
        blocklist.add("192.168.1.1");
        assertTrue(blocklist.matches("192.168.1.1"));
        assertEquals(1, blocklist.length());

        // Test adding the same address again
        blocklist.add("192.168.1.1");
        assertTrue(blocklist.matches("192.168.1.1"));
        assertEquals(1, blocklist.length());
    }

    @Test
    public void testBlocklistAddSubnet() {
        assertFalse(blocklist.matches("10.0.0.1"));
        blocklist.add("10.0.0.0/8");
        assertTrue(blocklist.matches("10.0.0.1"));
        assertTrue(blocklist.matches("10.1.1.1"));
        assertFalse(blocklist.matches("192.168.1.1"));
    }

    @Test
    public void testBlocklistMultipleAddressesAndSubnets() {
        blocklist.add("192.168.1.1");
        blocklist.add("10.0.0.0/8");

        assertTrue(blocklist.matches("192.168.1.1"));
        assertTrue(blocklist.matches("10.0.0.1"));
        assertTrue(blocklist.matches("10.1.1.1"));
        assertFalse(blocklist.matches("172.16.0.1"));
        assertEquals(2, blocklist.length());
    }

    @Test
    public void testBlocklistInvalidIp() {
        blocklist.add("192.168.1.1");
        assertFalse(blocklist.matches("invalid_ip"));
    }

    @Test
    public void testBlocklistSubnetEdgeCases() {
        blocklist.add("192.168.1.0/24");

        assertTrue(blocklist.matches("192.168.1.255")); // Last address in the subnet
        assertTrue(blocklist.matches("192.168.1.0")); // First address in the subnet
        assertFalse(blocklist.matches("192.168.2.1")); // Outside the subnet
        assertTrue(blocklist.matches("192.168.1.128")); // Middle of the subnet
    }

    @Test
    public void testBlocklistIpv6() {
        blocklist.add("2001:0db8:85a3:0000:0000:8a2e:0370:7334");
        assertTrue(blocklist.matches("2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
        assertFalse(blocklist.matches("::1")); // Not in the blocklist

        blocklist.add("2001:0db8::/32");
        assertTrue(blocklist.matches("2001:0db8:abcd:0012:0000:0000:0000:0001"));
        assertFalse(blocklist.matches("2001:0db9::"));
    }

    @Test
    public void testBlocklistOverlappingSubnets() {
        blocklist.add("192.168.1.0/24"); // Covers 192.168.1.0 to 192.168.1.255
        blocklist.add("192.168.1.128/25"); // Covers 192.168.1.128 to 192.168.1.255

        assertTrue(blocklist.matches("192.168.1.130")); // Inside both subnets
        assertTrue(blocklist.matches("192.168.1.0")); // Inside first subnet
        assertTrue(blocklist.matches("192.168.1.127")); // Inside first subnet, outside second
        assertTrue(blocklist.matches("192.168.1.255")); // Last address in both subnets
        assertFalse(blocklist.matches("192.168.2.1")); // Outside both subnets
    }

    @Test
    public void testBlocklistMixedAddressTypes() {
        blocklist.add("192.168.1.1");
        blocklist.add("2001:0db8:85a3:0000:0000:8a2e:0370:7334");

        assertTrue(blocklist.matches("192.168.1.1"));
        assertTrue(blocklist.matches("2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
        assertFalse(blocklist.matches("192.168.1.2")); // Different IPv4
        assertFalse(blocklist.matches("2001:0db8:85a3:0000:0000:8a2e:0370:7335")); // Different IPv6
    }

    @Test
    public void testBlocklistSubnetWithSingleIp() {
        blocklist.add("192.168.1.1/32"); // Single IP subnet

        assertTrue(blocklist.matches("192.168.1.1"));
        assertFalse(blocklist.matches("192.168.1.2")); // Outside the subnet
    }

    @Test
    public void testBlocklistSubnetWithSubnet2() {
        blocklist.add("192.168.2.1/24"); // Single IP subnet
        blocklist.add(null); // null is ignored

        assertTrue(blocklist.matches("192.168.2.1"));
        assertTrue(blocklist.matches("192.168.2.2"));
    }
}
