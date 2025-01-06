package dev.aikido.agent_api.helpers.net;

import java.util.regex.Pattern;

/**
 * Validates IP Addresses
 * Copied over from : https://github.com/validatorjs/validator.js/blob/master/src/lib/isIP.js
 * 11.3.  Examples
 *
 *    The following addresses
 *
 *              fe80::1234 (on the 1st link of the node)
 *              ff02::5678 (on the 5th link of the node)
 *              ff08::9abc (on the 10th organization of the node)
 *
 *    would be represented as follows:
 *
 *              fe80::1234%1
 *              ff02::5678%5
 *              ff08::9abc%10
 *
 *    (Here we assume a natural translation from a zone index to the
 *    <zone_id> part, where the Nth zone of any scope is translated into
 *    "N".)
 *
 *    If we use interface names as <zone_id>, those addresses could also be
 *    represented as follows:
 *
 *             fe80::1234%ne0
 *             ff02::5678%pvc1.3
 *             ff08::9abc%interface10
 *
 *    where the interface "ne0" belongs to the 1st link, "pvc1.3" belongs
 *    to the 5th link, and "interface10" belongs to the 10th organization.
 */
public class IPValidator {

    private static final String IPv4SegmentFormat = "(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])";
    private static final String IPv4AddressFormat = String.format("(%s\\.){3}%s", IPv4SegmentFormat, IPv4SegmentFormat);
    private static final Pattern IPv4AddressPattern = Pattern.compile(String.format("^%s$", IPv4AddressFormat));

    private static final String IPv6SegmentFormat = "(?:[0-9a-fA-F]{1,4})";
    private static final Pattern IPv6AddressPattern = Pattern.compile(
            "^(" +
                    String.format("(?:(%s):){7}(?:(%s)|:)|", IPv6SegmentFormat, IPv6SegmentFormat) +
                    String.format("(?:(%s):){6}(?:(%s)|(:%s)|:)|", IPv6SegmentFormat, IPv4AddressFormat, IPv6SegmentFormat) +
                    String.format("(?:(%s):){5}(?::%s|(:%s){1,2}|:)|", IPv6SegmentFormat, IPv4AddressFormat, IPv6SegmentFormat) +
                    String.format("(?:(%s):){4}(?:(:%s)?%s|(:%s){1,3}|:)|", IPv6SegmentFormat, IPv6SegmentFormat, IPv4AddressFormat, IPv6SegmentFormat) +
                    String.format("(?:(%s):){3}(?:(:%s){0,2}%s|(:%s){1,4}|:)|", IPv6SegmentFormat, IPv6SegmentFormat, IPv4AddressFormat, IPv6SegmentFormat) +
                    String.format("(?:(%s):){2}(?:(:%s){0,3}%s|(:%s){1,5}|:)|", IPv6SegmentFormat, IPv6SegmentFormat, IPv4AddressFormat, IPv6SegmentFormat) +
                    String.format("(?:(%s):){1}(?:(:%s){0,4}%s|(:%s){1,6}|:)|", IPv6SegmentFormat, IPv6SegmentFormat, IPv4AddressFormat, IPv6SegmentFormat) +
                    String.format("(?::((?::%s){0,5}:%s|(?::%s){1,7}|:))", IPv6SegmentFormat, IPv4AddressFormat, IPv6SegmentFormat) +
                    ")(%[0-9a-zA-Z-.:]{1,})?$"
    );

    public static boolean isIP(String str, String version) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        if (version == null || version.isEmpty()) {
            return isIP(str, "4") || isIP(str, "6");
        }
        if (version.equals("4")) {
            return IPv4AddressPattern.matcher(str).matches();
        }
        if (version.equals("6")) {
            return IPv6AddressPattern.matcher(str).matches();
        }
        return false;
    }
    public static boolean isIP(String str) {
        return isIP(str, null);
    }
}
