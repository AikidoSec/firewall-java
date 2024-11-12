package dev.aikido.agent_api.helpers.url;

import java.util.regex.Pattern;

import static dev.aikido.agent_api.helpers.patterns.LooksLikeASecret.looksLikeASecret;
import static dev.aikido.agent_api.helpers.patterns.LooksLikeValidIpAddress.isValidIpAddress;
import static dev.aikido.agent_api.helpers.url.UrlParser.tryParseUrlPath;

public final class BuildRouteFromUrl {
    private BuildRouteFromUrl() {}

    private static final Pattern UUID_REGEX = Pattern.compile(
            "[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}|00000000-0000-0000-0000-000000000000|ffffffff-ffff-ffff-ffff-ffffffffffff",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern NUMBER_REGEX = Pattern.compile("^\\d+$");
    private static final Pattern DATE_REGEX = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}|\\d{2}-\\d{2}-\\d{4}$");
    private static final Pattern EMAIL_REGEX = Pattern.compile(
            "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$");
    private static final Pattern HASH_REGEX = Pattern.compile("^(?:[a-f0-9]{32}|[a-f0-9]{40}|[a-f0-9]{64}|[a-f0-9]{128})$", Pattern.CASE_INSENSITIVE);
    private static final int[] HASH_LENGTHS = {32, 40, 64, 128};

    public static String buildRouteFromUrl(String url) {
        String path = tryParseUrlPath(url);

        if (path == null) {
            return null;
        }

        String[] segments = path.split("/");
        StringBuilder route = new StringBuilder();

        for (String segment : segments) {
            route.append(replaceUrlSegmentWithParam(segment));
            route.append("/");
        }

        if (route.isEmpty()) {
            return "/";
        }

        if (route.charAt(route.length() - 1) == '/') {
            return route.substring(0, route.length() - 1);
        }

        return route.toString();
    }

    private static String replaceUrlSegmentWithParam(String segment) {
        if (segment == null || segment.isEmpty()) {
            return segment;
        }

        char firstChar = segment.charAt(0);
        boolean startsWithNumber = Character.isDigit(firstChar);

        if (startsWithNumber && NUMBER_REGEX.matcher(segment).matches()) {
            return ":number";
        } else if (segment.length() == 36 && UUID_REGEX.matcher(segment).matches()) {
            return ":uuid";
        } else if (startsWithNumber && DATE_REGEX.matcher(segment).matches()) {
            return ":date";
        } else if (segment.contains("@") && EMAIL_REGEX.matcher(segment).matches()) {
            return ":email";
        } else if (isValidIpAddress(segment)) {
            return ":ip";
        } else if (isHash(segment)) {
            return ":hash";
        } else if (looksLikeASecret(segment)) {
            return ":secret";
        }

        return segment;
    }

    private static boolean isHash(String segment) {
        if (HASH_REGEX.matcher(segment).matches()) {
            for (int length : HASH_LENGTHS) {
                if (segment.length() == length) {
                    return true;
                }
            }
        }
        return false;
    }
}