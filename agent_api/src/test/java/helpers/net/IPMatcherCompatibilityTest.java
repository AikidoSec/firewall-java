package helpers.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.aikido.agent_api.helpers.net.IPList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class IPMatcherCompatibilityTest {
  @Test
  void matchesSingleIPv4Addresses() {
    IPList matcher = new IPList(List.of(
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
        "192.168.0.255/32"));

    assertFalse(matcher.matches("192.168.0.254"));
    assertFalse(matcher.matches("192.168.0.1"));
    assertTrue(matcher.matches("192.168.0.255"));
    assertTrue(matcher.matches("192.168.0.24"));
  }

  @Test
  void summarizesRangesAndContainedAddresses() {
    IPList matcher = new IPList(List.of(
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
        "192.168.0.255/32"));

    assertEquals(1, matcher.length());
    assertTrue(matcher.matches("192.168.0.254"));
    assertTrue(matcher.matches("192.168.0.234"));
    assertFalse(matcher.matches("10.0.0.1"));
  }

  @Test
  void ignoresInvalidRanges() {
    IPList matcher = new IPList(List.of(
        "192.168.0.0/24",
        "foobar",
        "0.a.0.0/32",
        "123.123.123.123/1999",
        "",
        ",,,",
        "192.168.0.255"));

    assertTrue(matcher.matches("192.168.0.254"));
    assertTrue(matcher.matches("192.168.0.255"));
    assertTrue(matcher.matches("192.168.0.1/32"));
    assertFalse(matcher.matches("foobar"));
    assertFalse(matcher.matches(""));
    assertFalse(matcher.matches("1"));
    assertFalse(matcher.matches(null));
    assertFalse(matcher.matches("10.0.0.1"));
  }

  @Test
  void emptyMatcherNeverMatches() {
    IPList matcher = new IPList(List.of());

    assertFalse(matcher.matches("192.168.2.1"));
    assertFalse(matcher.matches("foobar"));
  }

  @Test
  void matchesIPv6RangesAndBracketedAddresses() {
    IPList matcher = new IPList(List.of(
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
        "2001:db9::abc"));

    assertTrue(matcher.matches("2001:db8::1"));
    assertFalse(matcher.matches("2001:db8::0"));
    assertTrue(matcher.matches("2001:db8::f"));
    assertTrue(matcher.matches("[2001:db8::f]"));
    assertFalse(matcher.matches("2001:db8::10"));
    assertTrue(matcher.matches("2002:db8::1"));
    assertTrue(matcher.matches("2002:db8::2f:2"));
    assertTrue(matcher.matches("2001:db9::abc"));
  }

  @Test
  void keepsIPv4AndIPv6AddressSpacesSeparate() {
    IPList matcher = new IPList(List.of("2002:db8::/32", "10.0.0.0/8"));

    assertFalse(matcher.matches("2001:db8::1"));
    assertTrue(matcher.matches("2002:db8::1"));
    assertTrue(matcher.matches("10.0.0.1"));
    assertTrue(matcher.matches("10.0.0.255"));
    assertFalse(matcher.matches("192.168.1.1"));
  }

  @Test
  void supportsIncrementalAddsWithoutMutatingPublishedMatcherState() {
    IPList matcher = new IPList();

    assertFalse(matcher.matches("2002:db8::1"));
    matcher.add("2002:db8::/32");
    matcher.add("10.0.0.0/8");

    assertFalse(matcher.matches("2001:db8::1"));
    assertTrue(matcher.matches("2002:db8::1"));
    assertTrue(matcher.matches("10.0.0.1"));
    assertFalse(matcher.matches("192.168.1.1"));
  }

  @Test
  void preservesEmbeddedIPv4AddressForms() {
    IPList matcher = new IPList(List.of(
        "64:ff9b::192.0.2.1",
        "::ffff:192.0.2.1",
        "::ffff:127.0.0.1",
        "::ffff:0.0.0.0",
        "::ffff:0:0:0:0",
        "192.0.2.55"));

    assertTrue(matcher.matches("64:ff9b::c000:201"));
    assertFalse(matcher.matches("::ffff:192.0.2.1garbage"));
    assertFalse(matcher.matches("::ffff:192.0.2.1abc"));
    assertTrue(matcher.matches("::ffff:192.0.2.0x1"));
    assertTrue(matcher.matches("[::ffff:127.0.0.1]"));
    assertTrue(matcher.matches("::ffff:7f00:1"));
    assertTrue(matcher.matches("::ffff:0.0.0.0"));
    assertTrue(matcher.matches("::ffff:0:0:0:0"));
    assertTrue(matcher.matches("127.0.0.1"));
    assertTrue(matcher.matches("::ffff:192.0.2.55"));
    assertFalse(matcher.matches("::ffff:123"));
  }

  @ParameterizedTest
  @MethodSource("cidrCases")
  void appliesEveryIPv4CidrBoundary(String network, String address, boolean expected) {
    assertEquals(expected, new IPList(List.of(network)).matches(address));
  }

  static Stream<Arguments> cidrCases() {
    return Stream.of(
        Arguments.of("123.2.0.2/0", "1.1.1.1", true),
        Arguments.of("123.2.0.2/1", "1.1.1.1", true),
        Arguments.of("123.2.0.2/2", "1.1.1.1", false),
        Arguments.of("123.2.0.2/3", "123.3.0.1", true),
        Arguments.of("123.2.0.2/4", "123.3.0.1", true),
        Arguments.of("123.2.0.2/5", "123.3.0.1", true),
        Arguments.of("123.2.0.2/6", "123.3.0.1", true),
        Arguments.of("123.2.0.2/7", "123.3.0.1", true),
        Arguments.of("123.2.0.2/8", "123.3.0.1", true),
        Arguments.of("123.2.0.2/9", "123.3.0.1", true),
        Arguments.of("123.2.0.2/10", "123.3.0.1", true),
        Arguments.of("123.2.0.2/11", "123.3.0.1", true),
        Arguments.of("123.2.0.2/12", "123.3.0.1", true),
        Arguments.of("123.2.0.2/13", "123.3.0.1", true),
        Arguments.of("123.2.0.2/14", "123.3.0.1", true),
        Arguments.of("123.2.0.2/15", "123.3.0.1", true),
        Arguments.of("123.2.0.2/16", "123.3.0.1", false),
        Arguments.of("123.2.0.2/17", "123.2.0.1", true),
        Arguments.of("123.2.0.2/18", "123.2.0.1", true),
        Arguments.of("123.2.0.2/19", "123.2.0.1", true),
        Arguments.of("123.2.0.2/20", "123.2.0.1", true),
        Arguments.of("123.2.0.2/21", "123.2.0.1", true),
        Arguments.of("123.2.0.2/22", "123.2.0.1", true),
        Arguments.of("123.2.0.2/23", "123.2.0.1", true),
        Arguments.of("123.2.0.2/24", "123.2.0.1", true),
        Arguments.of("123.2.0.2/25", "123.2.0.1", true),
        Arguments.of("123.2.0.2/26", "123.2.0.1", true),
        Arguments.of("123.2.0.2/27", "123.2.0.1", true),
        Arguments.of("123.2.0.2/29", "123.2.0.1", true),
        Arguments.of("123.2.0.2/30", "123.2.0.1", true),
        Arguments.of("123.2.0.2/31", "123.2.0.1", false),
        Arguments.of("123.2.0.2/32", "123.2.0.2", true));
  }

  @Test
  void matchesNetworksOnlyWhenTheStoredNetworkContainsThem() {
    IPList matcher = new IPList(List.of("192.168.0.123/24", "2001:db8:1::beef/48"));

    assertTrue(matcher.matches("192.168.0.128/25"));
    assertFalse(matcher.matches("192.168.0.0/16"));
    assertTrue(matcher.matches("2001:db8:1:ffff::1"));
    assertFalse(matcher.matches("2001:db8:2::1"));
  }

  @Test
  void allowsBothAddressFamiliesWithZeroLengthPrefixes() {
    IPList matcher = new IPList(List.of("0.0.0.0/0", "::/0"));

    assertTrue(matcher.matches("1.2.3.4"));
    assertTrue(matcher.matches("::1"));
    assertTrue(matcher.matches("::ffff:1234"));
    assertTrue(matcher.matches("2002:db8::1"));
    assertTrue(matcher.matches("255.255.255.255"));
  }

  @Test
  void sortsAndSummarizesUnorderedNetworks() {
    IPList matcher = new IPList(List.of(
        "2001:db8:0:3::/64",
        "10.0.3.0/24",
        "2001:db8:0:1::/64",
        "10.0.1.0/24",
        "2001:db8:0:2::/64",
        "10.0.0.0/24",
        "2001:db8:0:0::/64",
        "10.0.2.0/24"));

    assertEquals(2, matcher.length());
    assertTrue(matcher.matches("10.0.3.255"));
    assertTrue(matcher.matches("2001:db8:0:3:ffff:ffff:ffff:ffff"));
    assertFalse(matcher.matches("10.0.4.0"));
    assertFalse(matcher.matches("2001:db8:0:4::"));
  }

  @Test
  void mergesAdjacentRangesAtBothAddressSpaceBoundaries() {
    IPList matcher = new IPList(List.of("224.0.0.0/4", "240.0.0.0/4", "e000::/4", "f000::/4"));

    assertEquals(2, matcher.length());
    assertTrue(matcher.matches("224.0.0.1"));
    assertTrue(matcher.matches("255.255.255.255"));
    assertFalse(matcher.matches("223.255.255.255"));
    assertTrue(matcher.matches("e000::1"));
    assertTrue(matcher.matches("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"));
    assertFalse(matcher.matches("dfff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"));
  }

  @Test
  void preservesNodeParserPortAndBracketForms() {
    IPList ipv4 = new IPList(List.of("192.0.2.1"));
    assertTrue(ipv4.matches("192.0.2.1:80"));

    IPList ipv6 = new IPList(List.of("2001:db8::1"));
    assertTrue(ipv6.matches("[2001:db8::1]:443"));
    assertTrue(ipv6.matches("2001:db8::1p443"));
    assertTrue(ipv6.matches("2001:db8::1#443"));
    assertTrue(ipv6.matches("2001:db8::1.443"));
    assertFalse(ipv6.matches("2001:db8::2p443"));
  }

  @Test
  void preservesNodeParserCidrAndShorthandBehavior() {
    IPList matcher = new IPList(List.of(" 192.168.2.1/24suffix ", "2001:db8"));

    assertTrue(matcher.matches("192.168.2.200"));
    assertTrue(matcher.matches("2001:db8::"));
    assertFalse(new IPList(List.of("192.168.2.1/abcde")).matches("192.168.2.1"));
    assertFalse(new IPList(List.of("192.168.2.1/24/test")).matches("192.168.2.1"));
  }

  @Test
  void mapsIPv4MappedIPv6RequestsToIPv4Networks() {
    IPList addressMatcher = new IPList(List.of("192.0.2.1"));
    assertTrue(addressMatcher.matches("192.0.2.1"));
    assertTrue(addressMatcher.matches("::ffff:192.0.2.1"));
    assertTrue(addressMatcher.matches("::ffff:c000:201"));
    assertFalse(addressMatcher.matches("::ffff:192.0.2.2"));

    IPList rangeMatcher = new IPList(List.of("192.0.2.0/24"));
    assertTrue(rangeMatcher.matches("::ffff:192.0.2.1"));
    assertTrue(rangeMatcher.matches("::ffff:192.0.2.255"));
    assertFalse(rangeMatcher.matches("::ffff:192.0.3.1"));
  }

  @Test
  void retainsJavaCompatibilityForStoredMappedNetworks() {
    IPList addressMatcher = new IPList(List.of("::ffff:192.0.2.1"));
    assertTrue(addressMatcher.matches("192.0.2.1"));
    assertTrue(addressMatcher.matches("::ffff:192.0.2.1"));

    IPList rangeMatcher = new IPList(List.of("::ffff:10.0.0.0/104"));
    assertTrue(rangeMatcher.matches("10.1.2.3"));
    assertTrue(rangeMatcher.matches("::ffff:10.1.2.3"));
    assertFalse(rangeMatcher.matches("11.1.2.3"));
  }
}
