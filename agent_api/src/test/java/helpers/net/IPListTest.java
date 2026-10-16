package helpers.net;

import dev.aikido.agent_api.helpers.net.IPList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    @Test
    public void testBlocklistMatchesIPv4MappedIPv6() {
        blocklist.add("192.168.1.1");
        assertTrue(blocklist.matches("::ffff:192.168.1.1"));
        assertFalse(blocklist.matches("::ffff:192.168.1.2"));

        blocklist.add("10.0.0.0/8");
        assertTrue(blocklist.matches("::ffff:10.5.6.7"));
        assertTrue(blocklist.matches("::ffff:10.0.0.1"));
        assertFalse(blocklist.matches("::ffff:11.0.0.1"));
    }

    @Test
    public void testBlocklistIPv6OnlyIgnoresIPv4MappedMismatch() {
        blocklist.add("2001:db8::/32");
        assertTrue(blocklist.matches("2001:db8::1"));
        assertFalse(blocklist.matches("::ffff:192.168.1.1"));
        assertFalse(blocklist.matches("192.168.1.1"));
    }

    @Test
    public void testBlocklistStoredIPv4MappedMatchesIPv4Input() {
        blocklist.add("::ffff:23.45.67.89");
        assertTrue(blocklist.matches("::ffff:23.45.67.89"));
    }

    @Test
    public void testBlocklistAddInvalidIpIgnored() {
        blocklist.add("notanip");
        assertEquals(0, blocklist.length());
        assertFalse(blocklist.matches("192.168.1.1"));
    }

    @Test
    public void testBlocklistLengthEmpty() {
        assertEquals(0, blocklist.length());
    }

    @Test
    public void testBlocklistStoredIPv4MappedMatchesPlainIPv4() {
        blocklist.add("::ffff:23.45.67.89");
        assertTrue(blocklist.matches("23.45.67.89"));
        assertTrue(blocklist.matches("::ffff:23.45.67.89"));
        assertFalse(blocklist.matches("23.45.67.90"));
    }

    @Test
    public void testBlocklistStoredIPv4MappedCidrMatchesPlainIPv4() {
        blocklist.add("::ffff:10.0.0.0/104");
        assertTrue(blocklist.matches("10.1.2.3"));
        assertTrue(blocklist.matches("::ffff:10.1.2.3"));
        assertFalse(blocklist.matches("11.1.2.3"));
    }

    @Test
    public void testBulkConstructorMatchesLikeAdd() {
        List<String> networks = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            networks.add("192.0.2." + i);
        }
        networks.add("2001:db8::/32");
        networks.add("notanip");
        networks.add(null);
        IPList bulk = new IPList(networks);

        assertTrue(bulk.matches("192.0.2.77"));
        assertTrue(bulk.matches("2001:db8::1"));
        assertFalse(bulk.matches("192.0.3.1"));
        // 256 adjacent addresses are summarised into 192.0.2.0/24
        assertEquals(2, bulk.length());

        bulk.add("10.0.0.0/8");
        assertTrue(bulk.matches("10.1.2.3"));
        assertTrue(bulk.matches("192.0.2.77"));
        assertEquals(3, bulk.length());
    }

    @Test
    public void testBulkConstructorWithNullOrEmptyCollection() {
        assertEquals(0, new IPList(null).length());
        assertFalse(new IPList(null).matches("1.2.3.4"));
        assertFalse(new IPList(List.of()).matches("1.2.3.4"));
    }

    @Test
    public void testMatchesNeverThrows() {
        blocklist.add("0.0.0.0/0");
        blocklist.add("::/0");
        List<String> malformedLookups = Arrays.asList(
                null, "", " ", "abc", "1.2.3.4.5", "999.1.1.1", "1.2.3.401-", "::ffff:186.3.320-.183",
                "-", "*", "/8", "[", "[]", "%", "::%", "1::2::3", "\u0661.0.0.1");
        for (String ip : malformedLookups) {
            assertDoesNotThrow(() -> blocklist.matches(ip), String.valueOf(ip));
        }
        assertFalse(blocklist.matches(null));
        assertFalse(blocklist.matches(""));
        assertFalse(blocklist.matches("1.2.3.401-"));
    }

    @ParameterizedTest(name = "network {0}, lookup {1}: {2}")
    @CsvSource(
            nullValues = "null",
            value = {
                "192.0.2.0/24,  ::ffff:192.0.2.1,                         true",
                "192.0.2.0/24,  ::FFFF:192.0.2.1,                         true",
                "192.0.2.0/24,  ::ffff:c000:201,                          true",
                "192.0.2.0/24,  ::FFFF:C000:201,                          true",
                "192.0.2.0/24,  0:0:0:0:0:ffff:c000:201,                  true",
                "192.0.2.0/24,  0000:0000:0000:0000:0000:ffff:c000:0201,  true",
                "192.0.2.0/24,  [::ffff:192.0.2.1],                       true",
                "192.0.2.0/24,  [::ffff:c000:201],                        true",
                "192.0.2.0/24,  ::ffff:192.0.2.1%eth0,                    true",
                "192.0.2.0/24,  ::ffff:c000:201%2,                        true",
                "192.0.2.0/24,  ::ffff:192.0.3.1,                         false",
                "192.0.2.0/24,  ::ffff:c000:301,                          false",
                "192.0.2.0/24,  ::192.0.2.1,                              false",
                "192.0.2.0/24,  ::ffff:0:c000:201,                        false",
                "192.0.2.0/24,  64:ff9b::192.0.2.1,                       false",
                "192.0.2.1,     ::ffff:192.0.2.1,                         true",
                "192.0.2.1,     ::ffff:192.0.2.2,                         false",
                "0.0.0.0/0,     ::ffff:8.8.8.8,                           true",
                "192.0.2.0/24,  abc,                                      false",
                "192.0.2.0/24,  '',                                       false",
                "192.0.2.0/24,  null,                                     false",
            })
    public void testIPv4MappedLookupsMatchIPv4Networks(String network, String lookup, boolean expected) {
        assertMatchesWhenAddedAndWhenBulkLoaded(network, lookup, expected);
    }

    @ParameterizedTest(name = "network {0}, lookup {1}: {2}")
    @CsvSource({
        "::ffff:192.0.2.1,        192.0.2.1,         true",
        "::ffff:c000:201,         192.0.2.1,         true",
        "::FFFF:C000:201,         192.0.2.1,         true",
        "0:0:0:0:0:ffff:c000:201, 192.0.2.1,         true",
        "::ffff:192.0.2.1,        192.0.2.2,         false",
        "::ffff:192.0.2.1,        ::ffff:c000:201,   true",
        "::ffff:192.0.2.0/120,    192.0.2.255,       true",
        "::ffff:192.0.2.0/120,    192.0.3.0,         false",
        "::ffff:192.0.2.0/97,     128.0.0.0,         true",
        "::ffff:192.0.2.0/97,     127.255.255.255,   false",
        "::ffff:0:0/96,           8.8.8.8,           true",
        "::ffff:0:0/80,           8.8.8.8,           true",
        "::ffff:0:0/80,           ::ffff:8.8.8.8,    true",
        "::192.0.2.1,             192.0.2.1,         false",
        "64:ff9b::192.0.2.1,      192.0.2.1,         false",
        "::ffff:192.0.2.1/129,    192.0.2.1,         false",
        "::ffff:999.0.2.1,        192.0.2.1,         false",
    })
    public void testIPv4MappedEntriesMatchIPv4Lookups(String network, String lookup, boolean expected) {
        assertMatchesWhenAddedAndWhenBulkLoaded(network, lookup, expected);
    }

    @Test
    public void testIPv4MappedEntriesAreStoredAsIPv4AndInvalidOnesDropped() {
        IPList list = new IPList(List.of("::ffff:192.0.2.1", "::ffff:c000:200/127", "::ffff:999.0.2.1"));
        assertEquals(1, list.length());
        assertTrue(list.matches("192.0.2.0"));
    }

    private static void assertMatchesWhenAddedAndWhenBulkLoaded(String network, String lookup, boolean expected) {
        IPList added = new IPList();
        added.add(network);
        IPList bulkLoaded = new IPList(List.of(network));

        assertEquals(expected, added.matches(lookup), "add()");
        assertEquals(expected, bulkLoaded.matches(lookup), "bulk constructor");
    }
}
