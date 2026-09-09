package helpers.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.aikido.agent_api.helpers.net.IPList;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class IPMatcherEdgeCaseTest {
  @Test
  void treatsNullAndEntirelyInvalidInputsAsEmpty() {
    IPList matcher = new IPList(null);

    matcher.add(null);

    assertEquals(0, matcher.length());
    assertFalse(matcher.matches(null));
    assertFalse(matcher.matchesWithMappedCheck(null));
    assertEquals(0, new IPList(List.of("invalid", "", " ")).length());
  }

  @Test
  void rejectsNonMappedFallbackInputs() {
    IPList matcher = new IPList(List.of("192.0.2.1"));

    assertFalse(matcher.matchesWithMappedCheck("invalid"));
    assertFalse(matcher.matchesWithMappedCheck("192.0.2.2"));
    assertFalse(matcher.matchesWithMappedCheck("2001:db8::1"));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"10.*.*.*", "10.*.*", "10.*.1.*", "10.256.*.*", "10.16777216", "10.1.65536", "127.1", "127.0.1", "192.168.257", "256.1", "10..0.1"
      })
  void ignoresMalformedIPv4Networks(String network) {
    assertEquals(0, new IPList(List.of(network)).length());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "256.0.0.1",
        "1.2.3.4.5",
        "2001::db8::1",
        "1:2:3:4:5:6:7:8:9",
        "12345::1",
        "gggg::1",
        ":::",
        "::ffff:192.0.2.999",
        "1:2:3:4:5:6:7:192.0.2.1",
        "[2001:db8::1"
      })
  void rejectsMalformedLookupAddresses(String address) {
    IPList matcher = new IPList(List.of("0.0.0.0/0", "::/0"));

    assertFalse(matcher.matches(address));
  }

  @ParameterizedTest
  @ValueSource(strings = {"::ffff:0Xc0.0B0.0O2.1E+0", "::ffff:+192.-0.2.01", "::ffff:192. .2.1"})
  void supportsNodeCompatibleEmbeddedIPv4Numbers(String address) {
    assertTrue(new IPList(List.of("::ffff:c000:201")).matches(address));
  }

  @Test
  void supportsEmbeddedIPv4InTheLeftHalfOfIPv6Addresses() {
    IPList matcher = new IPList(List.of("64:ff9b:c000:201::"));

    assertTrue(matcher.matches("64:ff9b:192.0.2.1::"));
  }

  @Test
  void compactsIncrementalNetworksWhileRetainingBothAddressFamilies() {
    IPList matcher = new IPList(List.of("10.0.0.0/25", "2001:db8::/65"));

    matcher.add("10.0.0.128/25");
    matcher.add("2001:db8:0:0:8000::/65");

    assertEquals(2, matcher.length());
    assertTrue(matcher.matches("10.0.0.255"));
    assertFalse(matcher.matches("10.0.1.0"));
    assertTrue(matcher.matches("2001:db8:0:0:ffff:ffff:ffff:ffff"));
    assertFalse(matcher.matches("2001:db8:0:1::"));
  }

  @Test
  void retainsMoreThanSixteenIPv4Networks() {
    List<String> networks =
        IntStream.range(0, 20).mapToObj(index -> "198.51.100." + index * 2).toList();

    IPList matcher = new IPList(networks);

    assertEquals(20, matcher.length());
    assertTrue(matcher.matches("198.51.100.0"));
    assertTrue(matcher.matches("198.51.100.38"));
    assertFalse(matcher.matches("198.51.100.39"));
  }
}
