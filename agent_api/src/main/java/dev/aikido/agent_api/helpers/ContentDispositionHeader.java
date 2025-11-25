/*!
 * Copied from https://github.com/jshttp/content-disposition/blob/master/index.js
 * Copyright(c) 2014-2017 Douglas Christopher Wilson
 * MIT Licensed
 */


package dev.aikido.agent_api.helpers;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

public class ContentDispositionHeader {

    // Regular expressions as static final strings
    private static final String HEX_ESCAPE_REPLACE_REGEXP = "%([0-9A-Fa-f]{2})";
    private static final String NON_LATIN1_REGEXP = "[^\\x20-\\x7e\\xa0-\\xff]";
    private static final String QESC_REGEXP = "\\\\\\([\\u0000-\\u007f])";
    private static final String PARAM_REGEXP = ";[\\x09\\x20]*([!#$%&'*+.0-9A-Z^_`a-z|~-]+)[\\x09\\x20]*=(?:[\\x09\\x20]*\"(?:[\\x20!\\x23-\\x5b\\x5d-\\x7e\\x80-\\xff]|\\\\[\\x20-\\x7e])*\"|[\\x09\\x20]*[!#$%&'*+.0-9A-Z^_`a-z|~-]+)[\\x09\\x20]*)";
    private static final String EXT_VALUE_REGEXP = "^([A-Za-z0-9!#$%&+\\-^_`{}~]+)'(?:[A-Za-z]{2,3}(?:-[A-Za-z]{3}){0,3}|[A-Za-z]{4,8}|)'((?:%[0-9A-Fa-f]{2}|[A-Za-z0-9!#$&+.^_`|~-])+)$";
    private static final String DISPOSITION_TYPE_REGEXP = "^([!#$%&'*+.0-9A-Z^_`a-z|~-]+)[\\x09\\x20]*(?:$|;)";

    private static String decodeField(String str) {
        Pattern pattern = Pattern.compile(EXT_VALUE_REGEXP);
        Matcher matcher = pattern.matcher(str);

        if (!matcher.find()) {
            throw new IllegalArgumentException("invalid extended field value");
        }

        String charset = matcher.group(1).toLowerCase();
        String encoded = matcher.group(2);
        String value;

        // to binary string
        String binary = replaceAll(encoded, result -> pDecode(result.group(1)));

        value = switch (charset) {
            case "iso-8859-1" -> getLatin1(binary);
            case "utf-8", "utf8" -> new String(binary.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            default -> throw new IllegalArgumentException("unsupported charset in extended field");
        };

        return value;
    }

    private static String getLatin1(String val) {
        // simple Unicode -> ISO-8859-1 transformation
        return val.replaceAll(NON_LATIN1_REGEXP, "?");
    }

    public record ParseResult(String type, Map<String, String> params) {}

    public static ParseResult parse(String string) {
        if (string == null || string.isEmpty()) {
            throw new IllegalArgumentException("argument string is required");
        }

        Pattern pattern = Pattern.compile(DISPOSITION_TYPE_REGEXP);
        Matcher matcher = pattern.matcher(string);

        if (!matcher.find()) {
            throw new IllegalArgumentException("invalid type format");
        }

        // normalize type
        int index = matcher.end();
        String type = matcher.group(1).toLowerCase();

        String key;
        List<String> names = new ArrayList<>();
        Map<String, String> params = new HashMap<>();
        String value;

        // calculate index to start at
        pattern = Pattern.compile(PARAM_REGEXP);
        matcher = pattern.matcher(string);
        matcher.region(index, string.length());

        // match parameters
        while (matcher.find()) {
            if (matcher.start() != index) {
                throw new IllegalArgumentException("invalid parameter format");
            }

            index = matcher.end();
            key = matcher.group(1).toLowerCase();
            value = matcher.group(2);

            if (names.contains(key)) {
                throw new IllegalArgumentException("invalid duplicate parameter");
            }

            names.add(key);

            if (key.endsWith("*")) {
                // decode extended value
                key = key.substring(0, key.length() - 1);
                value = decodeField(value);

                // overwrite existing value
                params.put(key, value);
                continue;
            }

            if (params.containsKey(key)) {
                continue;
            }

            if (value.startsWith("\"")) {
                // remove quotes and escapes
                value = value
                    .substring(1, value.length() - 1)
                    .replaceAll(QESC_REGEXP, "$1");
            }

            params.put(key, value);
        }

        if (index != -1 && index != string.length()) {
            throw new IllegalArgumentException("invalid parameter format");
        }

        return new ParseResult(type, params);
    }

    private static String pDecode(String hex) {
        return String.valueOf((char) Integer.parseInt(hex, 16));
    }

    private static String replaceAll(String input, Replacer replacer) {
        Pattern pattern = Pattern.compile(ContentDispositionHeader.HEX_ESCAPE_REPLACE_REGEXP);
        Matcher matcher = pattern.matcher(input);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            matcher.appendReplacement(sb, replacer.replace(matcher));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    interface Replacer {
        String replace(MatchResult result);
    }
}
