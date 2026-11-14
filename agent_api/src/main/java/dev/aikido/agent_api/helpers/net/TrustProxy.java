package dev.aikido.agent_api.helpers.net;

import inet.ipaddr.AddressStringParameters;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.IPAddressStringParameters;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class TrustProxy {
  private static volatile Config config;
  private static final IPAddressStringParameters RANGE_PARAMETERS =
      new IPAddressStringParameters.Builder()
          .allowEmpty(false)
          .allowAll(false)
          .allowSingleSegment(false)
          .allow_inet_aton(false)
          .allowMask(false)
          .allowPrefixOnly(false)
          .setRangeOptions(AddressStringParameters.RangeParameters.NO_RANGE)
          .toParams();

  private TrustProxy() {}

  public interface Config {}

  public record BooleanConfig(boolean value) implements Config {}

  public record CountConfig(int count) implements Config {}

  public record CIDRConfig(IPList ranges) implements Config {}

  public static void clearCache() {
    config = null;
  }

  public static Config getConfig() {
    Config current = config;
    if (current == null) {
      current = parse(System.getenv("AIKIDO_TRUST_PROXY"));
      config = current;
    }
    return current;
  }

  public static boolean isTrustedProxyRequest(String remoteAddress) {
    Config current = getConfig();
    if (current instanceof BooleanConfig booleanConfig) {
      return booleanConfig.value();
    }
    if (current instanceof CIDRConfig cidrConfig) {
      return remoteAddress != null && cidrConfig.ranges().matches(remoteAddress);
    }
    return true;
  }

  private static Config parse(String value) {
    if (value == null || value.isEmpty()) {
      return new BooleanConfig(true);
    }
    if (value.matches("[0-9]+")) {
      int count;
      try {
        count = Integer.parseInt(value);
      } catch (NumberFormatException ignored) {
        count = Integer.MAX_VALUE;
      }
      if (count > 0) {
        return new CountConfig(count);
      }
    }
    if (List.of("true", "1", "yes", "y", "on").contains(value.toLowerCase(Locale.ROOT))) {
      return new BooleanConfig(true);
    }
    if (value.contains(".") || value.contains(":")) {
      List<String> ranges = Arrays.stream(value.split(","))
          .map(String::trim)
          .filter(TrustProxy::isValidRange)
          .toList();
      return new CIDRConfig(new IPList(ranges));
    }
    return new BooleanConfig(false);
  }

  private static boolean isValidRange(String range) {
    return new IPAddressString(range, RANGE_PARAMETERS).isValid();
  }
}
