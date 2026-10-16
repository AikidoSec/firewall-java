package helpers.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.aikido.agent_api.helpers.net.IPMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

// Port of zen-internals src/ip_matcher/ip_matcher_test.rs and the matcher cases of src/ffi_bindings/ip_matcher_test.rs.
public class IPMatcherTest {
    private static IPMatcher matcherOf(String... networks) {
        return new IPMatcher(List.of(networks));
    }

    private static void assertMatches(IPMatcher matcher, String... lookups) {
        for (String lookup : lookups) {
            assertTrue(matcher.has(lookup), "expected a match for \"" + lookup + "\"");
        }
    }

    private static void assertNoMatch(IPMatcher matcher, String... lookups) {
        for (String lookup : lookups) {
            assertFalse(matcher.has(lookup), "expected no match for \"" + lookup + "\"");
        }
    }

    // expand("192.168.0.%d/32", 3, 24) returns ["192.168.0.3/32", "192.168.0.24/32"]
    private static List<String> expand(String template, int... values) {
        return Arrays.stream(values).mapToObj(value -> String.format(Locale.ROOT, template, value)).toList();
    }

    private static List<String> expandRange(String template, int first, int last) {
        return expand(template, IntStream.rangeClosed(first, last).toArray());
    }

    @Test
    public void memorySizeIncludesNetworkStorage() {
        IPMatcher empty = matcherOf();
        IPMatcher matcher = matcherOf("192.0.2.1");

        assertTrue(matcher.memorySize() > empty.memorySize());
    }

    @Test
    public void memorySizeOnlyCountsSummarizedNetworks() {
        IPMatcher summarized = new IPMatcher(expandRange("192.0.2.%d", 0, 255));
        IPMatcher single = matcherOf("192.0.2.0/24");

        assertEquals(single.memorySize(), summarized.memorySize());
    }

    @Test
    public void rejectedCountIsZeroWhenEveryNetworkIsValid() {
        IPMatcher matcher = matcherOf("10.0.0.0/8", "2001:db8::/32", "192.0.2.1");

        assertEquals(0, matcher.rejectedCount());
        assertEquals(0, matcherOf().rejectedCount());
    }

    @Test
    public void rejectedCountDoesCountInvalidNetworks() {
        IPMatcher matcher = matcherOf("10.0.0.0/8", "not-an-ip", "", "999.1.1.1", "2001:db8::/32");

        assertEquals(3, matcher.rejectedCount());
        assertMatches(matcher, "10.1.2.3", "2001:db8::1");
    }

    @Test
    public void rejectedCountDoesNotCountValidNetworksCoveredByAnotherNetwork() {
        List<String> networks = new ArrayList<>(expandRange("192.0.2.%d", 0, 255));
        networks.addAll(List.of("192.0.2.0/24", "192.0.2.0/24"));

        assertEquals(0, new IPMatcher(networks).rejectedCount());
    }

    @Test
    public void checkWithSingleIpv4s() {
        IPMatcher matcher = new IPMatcher(
                expand("192.168.0.%d/32", 0, 3, 24, 52, 123, 124, 125, 170, 171, 222, 234, 255));

        assertMatches(matcher, "192.168.0.255", "192.168.0.24");
        assertNoMatch(matcher, "192.168.0.254", "192.168.0.1");
    }

    @Test
    public void itWorksWithRanges() {
        List<String> networks = new ArrayList<>(List.of("192.168.0.0/24"));
        networks.addAll(expand("192.168.0.%d/32", 3, 24, 52, 123, 124, 125, 170, 171, 222, 234, 255));
        IPMatcher matcher = new IPMatcher(networks);

        assertMatches(matcher, "192.168.0.254", "192.168.0.234");
        assertNoMatch(matcher, "10.0.0.1");
    }

    @Test
    public void itWorksWithInvalidRanges() {
        List<String> networks = new ArrayList<>(List.of("192.168.0.0/24"));
        networks.addAll(expand("192.168.0.%d/32", 3, 24, 52, 124, 125, 170, 171, 222, 234));
        networks.add("192.168.0.255");
        networks.addAll(List.of("foobar", "0.a.0.0/32", "123.123.123.123/1999", "", ",,,"));
        IPMatcher matcher = new IPMatcher(networks);

        assertMatches(matcher, "192.168.0.254", "192.168.0.222", "192.168.0.1", "192.168.0.255", "192.168.0.1/32");
        assertNoMatch(matcher, "foobar", "10.0.0.1", "", "1");
    }

    @Test
    public void itAcceptsPortSuffixedIpv4Lookups() {
        assertMatches(matcherOf("192.0.2.1"), "192.0.2.1:80");
    }

    @Test
    public void itWorksWithEmptyRanges() {
        assertNoMatch(matcherOf(), "192.168.2.1", "foobar");
    }

    @Test
    public void itWorksWithIpv6Ranges() {
        List<String> networks = new ArrayList<>(List.of("2002:db8::/32"));
        networks.addAll(expandRange("2001:db8::%x/128", 0x1, 0xe));
        networks.addAll(List.of("[2001:db8::f]", "2001:db9::abc"));
        IPMatcher matcher = new IPMatcher(networks);

        assertMatches(
                matcher,
                "2001:db8::1",
                "2001:db8::f",
                "[2001:db8::f]",
                "2002:db8::1",
                "2002:db8::2f:2",
                "2001:db9::abc");
        assertNoMatch(matcher, "2001:db8::0", "2001:db8::10");
    }

    @Test
    public void itStripsZonesFromScopedIpv6Addresses() {
        IPMatcher matcher = matcherOf("fe80::/10", "::1/128");

        assertMatches(matcher, "fe80::1%eth0", "fe80::1%dummy0", "[fe80::1%eth0]", "::1%lo");
        assertNoMatch(matcher, "fe80::1%");
    }

    @Test
    public void mixIpv4AndIpv6() {
        IPMatcher matcher = matcherOf("2002:db8::/32", "10.0.0.0/8");

        assertMatches(matcher, "2002:db8::1", "10.0.0.1", "10.0.0.255");
        assertNoMatch(matcher, "2001:db8::1", "2001:db8::0", "192.168.1.1");
    }

    @Test
    public void strangeIps() {
        IPMatcher matcher = matcherOf("::ffff:0.0.0.0", "::ffff:0:0:0:0", "::ffff:127.0.0.1");

        assertMatches(matcher, "::ffff:0.0.0.0", "::ffff:127.0.0.1", "[::ffff:0.0.0.0]", "::ffff:0:0:0:0");
        assertNoMatch(matcher, "::ffff:123", "2001:db8::1");
    }

    @ParameterizedTest(name = "{0} matches {1}: {2}")
    @CsvSource({
        "123.2.0.2/0,  1.1.1.1,    true",
        "123.2.0.2/1,  1.1.1.1,    true",
        "123.2.0.2/2,  1.1.1.1,    false",
        "123.2.0.2/3,  123.3.0.1,  true",
        "123.2.0.2/4,  123.3.0.1,  true",
        "123.2.0.2/5,  123.3.0.1,  true",
        "123.2.0.2/6,  123.3.0.1,  true",
        "123.2.0.2/7,  123.3.0.1,  true",
        "123.2.0.2/8,  123.3.0.1,  true",
        "123.2.0.2/9,  123.3.0.1,  true",
        "123.2.0.2/10, 123.3.0.1,  true",
        "123.2.0.2/11, 123.3.0.1,  true",
        "123.2.0.2/12, 123.3.0.1,  true",
        "123.2.0.2/13, 123.3.0.1,  true",
        "123.2.0.2/14, 123.3.0.1,  true",
        "123.2.0.2/15, 123.3.0.1,  true",
        "123.2.0.2/16, 123.3.0.1,  false",
        "123.2.0.2/17, 123.2.0.1,  true",
        "123.2.0.2/18, 123.2.0.1,  true",
        "123.2.0.2/19, 123.2.0.1,  true",
        "123.2.0.2/20, 123.2.0.1,  true",
        "123.2.0.2/21, 123.2.0.1,  true",
        "123.2.0.2/22, 123.2.0.1,  true",
        "123.2.0.2/23, 123.2.0.1,  true",
        "123.2.0.2/24, 123.2.0.1,  true",
        "123.2.0.2/25, 123.2.0.1,  true",
        "123.2.0.2/26, 123.2.0.1,  true",
        "123.2.0.2/27, 123.2.0.1,  true",
        "123.2.0.2/29, 123.2.0.1,  true",
        "123.2.0.2/30, 123.2.0.1,  true",
        "123.2.0.2/31, 123.2.0.1,  false",
        "123.2.0.2/32, 123.2.0.2,  true",
    })
    public void differentCidrRanges(String network, String lookup, boolean expected) {
        assertEquals(expected, matcherOf(network).has(lookup));
    }

    @Test
    public void allowAllIps() {
        IPMatcher matcher = matcherOf("0.0.0.0/0", "::/0");

        assertMatches(
                matcher,
                "1.2.3.4",
                "::1",
                "::ffff:1234",
                "1.1.1.1",
                "2002:db8::1",
                "10.0.0.1",
                "10.0.0.255",
                "192.168.1.1");
    }

    @Test
    public void adjacent4RangesAtEndOfAddressSpace() {
        IPMatcher matcher = matcherOf("224.0.0.0/4", "240.0.0.0/4");

        assertMatches(matcher, "224.0.0.1", "240.0.0.1", "255.255.255.255");
        assertNoMatch(matcher, "223.255.255.255");
    }

    @Test
    public void matchesMixedNetworksAndIgnoresInvalidEntries() {
        IPMatcher matcher = matcherOf(
                "192.168.0.123/24",
                "2001:db8:1::beef/48",
                "[2001:db8::f]",
                "not-an-address",
                "0.a.0.0/32",
                "123.123.123.123/1999",
                "");

        assertMatches(
                matcher,
                "192.168.0.1",
                "192.168.0.42:443",
                "192.168.0.128/25",
                "2001:db8:1:ffff::1",
                "2001:db8::f",
                "[2001:db8::f]");
        assertNoMatch(
                matcher,
                "192.168.0.0/16",
                "192.168.1.1",
                "2001:db8:2::1",
                "not-an-address",
                "192.168.0.999",
                "");
    }

    @Test
    public void preservesEmbeddedIpv4AndMappedAddressForms() {
        IPMatcher matcher = matcherOf(
                "64:ff9b::192.0.2.1", "::ffff:127.0.0.1", "::ffff:0.0.0.0", "::ffff:0:0:0:0", "192.0.2.55");

        assertMatches(
                matcher,
                "64:ff9b::c000:201",
                "[::ffff:127.0.0.1]",
                "::ffff:7f00:1",
                "::ffff:0.0.0.0",
                "::ffff:0:0:0:0",
                "::ffff:192.0.2.55");
        assertNoMatch(matcher, "127.0.0.1", "::ffff:123");
    }

    @Test
    public void matchesIpv4MappedLookupsAgainstIpv4Networks() {
        IPMatcher matcher = matcherOf("192.0.2.55", "10.0.0.0/8", "2001:db8::/32");

        assertMatches(
                matcher,
                "::ffff:192.0.2.55",
                "[::ffff:192.0.2.55]",
                "::FFFF:192.0.2.55",
                "::ffff:c000:237",
                "0:0:0:0:0:ffff:c000:237",
                "::ffff:10.1.2.3",
                "192.0.2.55",
                "2001:db8::1");
        assertNoMatch(matcher, "::ffff:198.51.100.1", "::c000:237", "::ffff:0:c000:237", "64:ff9b::c000:237");
    }

    @Test
    public void mergesAdjacentRangesAtAddressSpaceBoundaries() {
        IPMatcher matcher = matcherOf("224.0.0.0/4", "240.0.0.0/4", "e000::/4", "f000::/4");

        assertMatches(matcher, "224.0.0.1", "255.255.255.255", "e000::1", "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff");
        assertNoMatch(matcher, "223.255.255.255", "dfff:ffff:ffff:ffff:ffff:ffff:ffff:ffff");
    }

    @Test
    public void matcherHandleLifecyclePreservesMatcherBehavior() {
        IPMatcher matcher = matcherOf("10.0.0.42/8", "2001:db8::/32", "invalid network");

        assertTrue(matcher.memorySize() > 0);
        assertEquals(1, matcher.rejectedCount());
        assertMatches(matcher, "10.255.255.255", "[2001:db8::1]", "::ffff:10.1.2.3");
        assertNoMatch(matcher, "192.0.2.1");
    }

    @Test
    public void lookupReportsNoMatchSeparatelyFromBoundaryErrors() {
        assertNoMatch(matcherOf(), "", "not-an-address");
    }

    @Test
    public void nullNetworksAreRejectedAndNullLookupsDoNotMatch() {
        IPMatcher matcher = new IPMatcher(Arrays.asList("10.0.0.0/8", null));

        assertEquals(1, matcher.rejectedCount());
        assertFalse(matcher.has(null));
        assertEquals(0, new IPMatcher(null).rejectedCount());
    }
}
