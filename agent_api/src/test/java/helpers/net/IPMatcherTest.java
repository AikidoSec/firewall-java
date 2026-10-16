package helpers.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.aikido.agent_api.helpers.net.IPMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

// Port of zen-internals src/ip_matcher/ip_matcher_test.rs and the matcher cases of src/ffi_bindings/ip_matcher_test.rs.
public class IPMatcherTest {
    private static void assertCases(List<String> networks, Object... cases) {
        IPMatcher matcher = new IPMatcher(networks);
        for (int index = 0; index < cases.length; index += 2) {
            String lookup = (String) cases[index];
            boolean expected = (Boolean) cases[index + 1];
            assertEquals(expected, matcher.has(lookup), "unexpected result for \"" + lookup + "\"");
        }
    }

    private static List<String> adjacentAddresses() {
        List<String> adjacent = new ArrayList<>();
        for (int last = 0; last <= 255; last++) {
            adjacent.add("192.0.2." + last);
        }
        return adjacent;
    }

    @Test
    public void memorySizeIncludesNetworkStorage() {
        IPMatcher empty = new IPMatcher(List.of());
        IPMatcher matcher = new IPMatcher(List.of("192.0.2.1"));

        assertTrue(matcher.memorySize() > empty.memorySize());
    }

    @Test
    public void memorySizeOnlyCountsSummarizedNetworks() {
        IPMatcher summarized = new IPMatcher(adjacentAddresses());
        IPMatcher single = new IPMatcher(List.of("192.0.2.0/24"));

        assertEquals(single.memorySize(), summarized.memorySize());
    }

    @Test
    public void rejectedCountIsZeroWhenEveryNetworkIsValid() {
        IPMatcher matcher = new IPMatcher(List.of("10.0.0.0/8", "2001:db8::/32", "192.0.2.1"));
        IPMatcher empty = new IPMatcher(List.of());

        assertEquals(0, matcher.rejectedCount());
        assertEquals(0, empty.rejectedCount());
    }

    @Test
    public void rejectedCountDoesCountInvalidNetworks() {
        IPMatcher matcher = new IPMatcher(List.of("10.0.0.0/8", "not-an-ip", "", "999.1.1.1", "2001:db8::/32"));

        assertEquals(3, matcher.rejectedCount());
        assertTrue(matcher.has("10.1.2.3"));
        assertTrue(matcher.has("2001:db8::1"));
    }

    @Test
    public void rejectedCountDoesNotCountValidNetworksCoveredByAnotherNetwork() {
        List<String> networks = adjacentAddresses();
        networks.addAll(List.of("192.0.2.0/24", "192.0.2.0/24"));
        IPMatcher matcher = new IPMatcher(networks);

        assertEquals(0, matcher.rejectedCount());
    }

    @Test
    public void checkWithSingleIpv4s() {
        assertCases(
                List.of(
                        "192.168.0.0/32",
                        "192.168.0.3/32",
                        "192.168.0.24/32",
                        "192.168.0.52/32",
                        "192.168.0.123/32",
                        "192.168.0.124/32",
                        "192.168.0.125/32",
                        "192.168.0.170/32",
                        "192.168.0.171/32",
                        "192.168.0.222/32",
                        "192.168.0.234/32",
                        "192.168.0.255/32"),
                "192.168.0.254",
                false,
                "192.168.0.1",
                false,
                "192.168.0.255",
                true,
                "192.168.0.24",
                true);
    }

    @Test
    public void itWorksWithRanges() {
        assertCases(
                List.of(
                        "192.168.0.0/24",
                        "192.168.0.3/32",
                        "192.168.0.24/32",
                        "192.168.0.52/32",
                        "192.168.0.123/32",
                        "192.168.0.124/32",
                        "192.168.0.125/32",
                        "192.168.0.170/32",
                        "192.168.0.171/32",
                        "192.168.0.222/32",
                        "192.168.0.234/32",
                        "192.168.0.255/32"),
                "192.168.0.254",
                true,
                "10.0.0.1",
                false,
                "192.168.0.234",
                true);
    }

    @Test
    public void itWorksWithInvalidRanges() {
        assertCases(
                List.of(
                        "192.168.0.0/24",
                        "192.168.0.3/32",
                        "192.168.0.24/32",
                        "192.168.0.52/32",
                        "foobar",
                        "0.a.0.0/32",
                        "123.123.123.123/1999",
                        "",
                        ",,,",
                        "192.168.0.124/32",
                        "192.168.0.125/32",
                        "192.168.0.170/32",
                        "192.168.0.171/32",
                        "192.168.0.222/32",
                        "192.168.0.234/32",
                        "192.168.0.255"),
                "192.168.0.254",
                true,
                "foobar",
                false,
                "192.168.0.222",
                true,
                "192.168.0.1",
                true,
                "10.0.0.1",
                false,
                "192.168.0.255",
                true,
                "",
                false,
                "1",
                false,
                "192.168.0.1/32",
                true);
    }

    @Test
    public void itAcceptsPortSuffixedIpv4Lookups() {
        assertCases(List.of("192.0.2.1"), "192.0.2.1:80", true);
    }

    @Test
    public void itWorksWithEmptyRanges() {
        assertCases(List.of(), "192.168.2.1", false, "foobar", false);
    }

    @Test
    public void itWorksWithIpv6Ranges() {
        assertCases(
                List.of(
                        "2002:db8::/32",
                        "2001:db8::1/128",
                        "2001:db8::2/128",
                        "2001:db8::3/128",
                        "2001:db8::4/128",
                        "2001:db8::5/128",
                        "2001:db8::6/128",
                        "2001:db8::7/128",
                        "2001:db8::8/128",
                        "2001:db8::9/128",
                        "2001:db8::a/128",
                        "2001:db8::b/128",
                        "2001:db8::c/128",
                        "2001:db8::d/128",
                        "2001:db8::e/128",
                        "[2001:db8::f]",
                        "2001:db9::abc"),
                "2001:db8::1",
                true,
                "2001:db8::0",
                false,
                "2001:db8::f",
                true,
                "[2001:db8::f]",
                true,
                "2001:db8::10",
                false,
                "2002:db8::1",
                true,
                "2002:db8::2f:2",
                true,
                "2001:db9::abc",
                true);
    }

    @Test
    public void itStripsZonesFromScopedIpv6Addresses() {
        assertCases(
                List.of("fe80::/10", "::1/128"),
                "fe80::1%eth0",
                true,
                "fe80::1%dummy0",
                true,
                "[fe80::1%eth0]",
                true,
                "::1%lo",
                true,
                "fe80::1%",
                false);
    }

    @Test
    public void mixIpv4AndIpv6() {
        assertCases(
                List.of("2002:db8::/32", "10.0.0.0/8"),
                "2001:db8::1",
                false,
                "2001:db8::0",
                false,
                "2002:db8::1",
                true,
                "10.0.0.1",
                true,
                "10.0.0.255",
                true,
                "192.168.1.1",
                false);
    }

    @Test
    public void strangeIps() {
        assertCases(
                List.of("::ffff:0.0.0.0", "::ffff:0:0:0:0", "::ffff:127.0.0.1"),
                "::ffff:0.0.0.0",
                true,
                "::ffff:127.0.0.1",
                true,
                "::ffff:123",
                false,
                "2001:db8::1",
                false,
                "[::ffff:0.0.0.0]",
                true,
                "::ffff:0:0:0:0",
                true);
    }

    @Test
    public void differentCidrRanges() {
        Object[][] tests = {
            {"123.2.0.2/0", "1.1.1.1", true},
            {"123.2.0.2/1", "1.1.1.1", true},
            {"123.2.0.2/2", "1.1.1.1", false},
            {"123.2.0.2/3", "123.3.0.1", true},
            {"123.2.0.2/4", "123.3.0.1", true},
            {"123.2.0.2/5", "123.3.0.1", true},
            {"123.2.0.2/6", "123.3.0.1", true},
            {"123.2.0.2/7", "123.3.0.1", true},
            {"123.2.0.2/8", "123.3.0.1", true},
            {"123.2.0.2/9", "123.3.0.1", true},
            {"123.2.0.2/10", "123.3.0.1", true},
            {"123.2.0.2/11", "123.3.0.1", true},
            {"123.2.0.2/12", "123.3.0.1", true},
            {"123.2.0.2/13", "123.3.0.1", true},
            {"123.2.0.2/14", "123.3.0.1", true},
            {"123.2.0.2/15", "123.3.0.1", true},
            {"123.2.0.2/16", "123.3.0.1", false},
            {"123.2.0.2/17", "123.2.0.1", true},
            {"123.2.0.2/18", "123.2.0.1", true},
            {"123.2.0.2/19", "123.2.0.1", true},
            {"123.2.0.2/20", "123.2.0.1", true},
            {"123.2.0.2/21", "123.2.0.1", true},
            {"123.2.0.2/22", "123.2.0.1", true},
            {"123.2.0.2/23", "123.2.0.1", true},
            {"123.2.0.2/24", "123.2.0.1", true},
            {"123.2.0.2/25", "123.2.0.1", true},
            {"123.2.0.2/26", "123.2.0.1", true},
            {"123.2.0.2/27", "123.2.0.1", true},
            {"123.2.0.2/29", "123.2.0.1", true},
            {"123.2.0.2/30", "123.2.0.1", true},
            {"123.2.0.2/31", "123.2.0.1", false},
            {"123.2.0.2/32", "123.2.0.2", true},
        };

        for (Object[] test : tests) {
            assertCases(List.of((String) test[0]), test[1], test[2]);
        }
    }

    @Test
    public void allowAllIps() {
        assertCases(
                List.of("0.0.0.0/0", "::/0"),
                "1.2.3.4",
                true,
                "::1",
                true,
                "::ffff:1234",
                true,
                "1.1.1.1",
                true,
                "2002:db8::1",
                true,
                "10.0.0.1",
                true,
                "10.0.0.255",
                true,
                "192.168.1.1",
                true);
    }

    @Test
    public void adjacent4RangesAtEndOfAddressSpace() {
        assertCases(
                List.of("224.0.0.0/4", "240.0.0.0/4"),
                "224.0.0.1",
                true,
                "240.0.0.1",
                true,
                "255.255.255.255",
                true,
                "223.255.255.255",
                false);
    }

    @Test
    public void matchesMixedNetworksAndIgnoresInvalidEntries() {
        assertCases(
                List.of(
                        "192.168.0.123/24",
                        "2001:db8:1::beef/48",
                        "[2001:db8::f]",
                        "not-an-address",
                        "0.a.0.0/32",
                        "123.123.123.123/1999",
                        ""),
                "192.168.0.1",
                true,
                "192.168.0.42:443",
                true,
                "192.168.0.128/25",
                true,
                "192.168.0.0/16",
                false,
                "192.168.1.1",
                false,
                "2001:db8:1:ffff::1",
                true,
                "2001:db8:2::1",
                false,
                "2001:db8::f",
                true,
                "[2001:db8::f]",
                true,
                "not-an-address",
                false,
                "192.168.0.999",
                false,
                "",
                false);
    }

    @Test
    public void preservesEmbeddedIpv4AndMappedAddressForms() {
        assertCases(
                List.of("64:ff9b::192.0.2.1", "::ffff:127.0.0.1", "::ffff:0.0.0.0", "::ffff:0:0:0:0", "192.0.2.55"),
                "64:ff9b::c000:201",
                true,
                "[::ffff:127.0.0.1]",
                true,
                "::ffff:7f00:1",
                true,
                "::ffff:0.0.0.0",
                true,
                "::ffff:0:0:0:0",
                true,
                "127.0.0.1",
                false,
                "::ffff:192.0.2.55",
                true,
                "::ffff:123",
                false);
    }

    @Test
    public void matchesIpv4MappedLookupsAgainstIpv4Networks() {
        assertCases(
                List.of("192.0.2.55", "10.0.0.0/8", "2001:db8::/32"),
                "::ffff:192.0.2.55",
                true,
                "[::ffff:192.0.2.55]",
                true,
                "::FFFF:192.0.2.55",
                true,
                "::ffff:c000:237",
                true,
                "0:0:0:0:0:ffff:c000:237",
                true,
                "::ffff:10.1.2.3",
                true,
                "::ffff:198.51.100.1",
                false,
                "::c000:237",
                false,
                "::ffff:0:c000:237",
                false,
                "64:ff9b::c000:237",
                false,
                "192.0.2.55",
                true,
                "2001:db8::1",
                true);
    }

    @Test
    public void mergesAdjacentRangesAtAddressSpaceBoundaries() {
        assertCases(
                List.of("224.0.0.0/4", "240.0.0.0/4", "e000::/4", "f000::/4"),
                "224.0.0.1",
                true,
                "255.255.255.255",
                true,
                "223.255.255.255",
                false,
                "e000::1",
                true,
                "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff",
                true,
                "dfff:ffff:ffff:ffff:ffff:ffff:ffff:ffff",
                false);
    }

    @Test
    public void matcherHandleLifecyclePreservesMatcherBehavior() {
        IPMatcher matcher = new IPMatcher(List.of("10.0.0.42/8", "2001:db8::/32", "invalid network"));

        assertTrue(matcher.memorySize() > 0);
        assertEquals(1, matcher.rejectedCount());
        assertTrue(matcher.has("10.255.255.255"));
        assertTrue(matcher.has("[2001:db8::1]"));
        assertTrue(matcher.has("::ffff:10.1.2.3"));
        assertFalse(matcher.has("192.0.2.1"));
    }

    @Test
    public void ffiRejectedCountIsZeroWhenEveryNetworkIsValid() {
        assertEquals(0, new IPMatcher(List.of("10.0.0.0/8", "2001:db8::/32")).rejectedCount());
        assertEquals(0, new IPMatcher(List.of()).rejectedCount());
    }

    @Test
    public void lookupReportsNoMatchSeparatelyFromBoundaryErrors() {
        IPMatcher matcher = new IPMatcher(List.of());

        assertFalse(matcher.has(""));
        assertFalse(matcher.has("not-an-address"));
    }

    @Test
    public void nullNetworksAreRejectedAndNullLookupsDoNotMatch() {
        IPMatcher matcher = new IPMatcher(Arrays.asList("10.0.0.0/8", null));

        assertEquals(1, matcher.rejectedCount());
        assertFalse(matcher.has(null));
        assertEquals(0, new IPMatcher(null).rejectedCount());
    }
}
