package dev.aikido.agent_api.helpers.patterns;

import java.util.regex.Pattern;

public class LooksLikeValidIpAddress {
    private static final Pattern IPV4_PATTERN = Pattern
            .compile("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

    private static final Pattern IPV6_PATTERN = Pattern
            .compile("^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");
    private static final Pattern IPV6_COMPRESSED_PATTERN = Pattern
            .compile("^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)$");

    private static boolean isValidIPv4(String ip) {
        return IPV4_PATTERN.matcher(ip).matches();
    }

    private static boolean isValidIPv6(String ip) {
        boolean matchesUncompressed = IPV6_PATTERN.matcher(ip).matches();
        if (matchesUncompressed) {
            return true;
        }
        return IPV6_COMPRESSED_PATTERN.matcher(ip).matches();
    }

    public static boolean isValidIpAddress(String segment) {
        return isValidIPv4(segment) || isValidIPv6(segment);
    }
}
