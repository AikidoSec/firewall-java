/*!
 * Copied from https://github.com/jshttp/content-disposition/blob/master/index.js
 * Copyright(c) 2014-2017 Douglas Christopher Wilson
 * MIT Licensed
 */


package dev.aikido.agent_api.helpers;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

public final class ContentDispositionHeader {
    private ContentDispositionHeader() {}
    private static final Pattern HEX_ESCAPE_REPLACE_REGEXP = Pattern.compile("%([0-9A-Fa-f]{2})");

    /**
     * RegExp to match non-latin1 characters.
     * @private
     */
    private static final Pattern NON_LATIN1_REGEXP = Pattern.compile("[^\\x20-\\x7e\\xa0-\\xff]");

    /**
     * RegExp to match quoted-pair in RFC 2616
     *
     * quoted-pair = "\" CHAR
     * CHAR        = <any US-ASCII character (octets 0 - 127)>
     */
    private static final Pattern QESC_REGEXP = Pattern.compile("\\\\([\\u0000-\\u007f])");

    /**
     * RegExp for various RFC 2616 grammar
     *
     * parameter     = token "=" ( token | quoted-string )
     * token         = 1*<any CHAR except CTLs or separators>
     * separators    = "(" | ")" | "<" | ">" | "@"
     *               | "," | ";" | ":" | "\" | <">
     *               | "/" | "[" | "]" | "?" | "="
     *               | "{" | "}" | SP | HT
     * quoted-string = ( <"> *(qdtext | quoted-pair ) <"> )
     * qdtext        = <any TEXT except <">>
     * quoted-pair   = "\" CHAR
     * CHAR          = <any US-ASCII character (octets 0 - 127)>
     * TEXT          = <any OCTET except CTLs, but including LWS>
     * LWS           = [CRLF] 1*( SP | HT )
     * CRLF          = CR LF
     * CR            = <US-ASCII CR, carriage return (13)>
     * LF            = <US-ASCII LF, linefeed (10)>
     * SP            = <US-ASCII SP, space (32)>
     * HT            = <US-ASCII HT, horizontal-tab (9)>
     * CTL           = <any US-ASCII control character (octets 0 - 31) and DEL (127)>
     * OCTET         = <any 8-bit sequence of data>
     */
    private static final Pattern PARAM_REGEXP = Pattern.compile(";[\\t ]*([!#$%&'*+.0-9A-Z^_`a-z|~-]+)[\\t ]*=[\\t ]*(\"(?:[\\x20!\\x23-\\x5b\\x5d-\\x7e\\x80-\\xff]|\\\\[\\x20-\\x7e])*\"|[!#$%&'*+.0-9A-Z^_`a-z|~-]+)[\\t ]*");

    /**
     * RegExp for various RFC 5987 grammar
     *
     * ext-value     = charset  "'" [ language ] "'" value-chars
     * charset       = "UTF-8" / "ISO-8859-1" / mime-charset
     * mime-charset  = 1*mime-charsetc
     * mime-charsetc = ALPHA / DIGIT
     *               / "!" / "#" / "$" / "%" / "&"
     *               / "+" / "-" / "^" / "_" / "`"
     *               / "{" / "}" / "~"
     * language      = ( 2*3ALPHA [ extlang ] )
     *               / 4ALPHA
     *               / 5*8ALPHA
     * extlang       = *3( "-" 3ALPHA )
     * value-chars   = *( pct-encoded / attr-char )
     * pct-encoded   = "%" HEXDIG HEXDIG
     * attr-char     = ALPHA / DIGIT
     *               / "!" / "#" / "$" / "&" / "+" / "-" / "."
     *               / "^" / "_" / "`" / "|" / "~"
     */
    private static final Pattern EXT_VALUE_REGEXP = Pattern.compile("^([A-Za-z0-9!#$%&+\\-^_`{}~]+)'(?:[A-Za-z]{2,3}(?:-[A-Za-z]{3}){0,3}|[A-Za-z]{4,8}|)'((?:%[0-9A-Fa-f]{2}|[A-Za-z0-9!#$&+.^_`|~-])+)$");

    /**
     * RegExp for various RFC 6266 grammar
     *
     * disposition-type = "inline" | "attachment" | disp-ext-type
     * disp-ext-type    = token
     * disposition-parm = filename-parm | disp-ext-parm
     * filename-parm    = "filename" "=" value
     *                  | "filename*" "=" ext-value
     * disp-ext-parm    = token "=" value
     *                  | ext-token "=" ext-value
     * ext-token        = <the characters in token, followed by "*">
     */
    private static final Pattern DISPOSITION_TYPE_REGEXP = Pattern.compile("^([!#$%&'*+.0-9A-Z^_`a-z|~-]+)[\\t ]*(?:$|;)");

    private static String decodeField(String str) {
        Matcher matcher = EXT_VALUE_REGEXP.matcher(str);

        if (!matcher.find()) {
            throw new IllegalArgumentException("invalid extended field value");
        }

        String charset = matcher.group(1).toLowerCase();
        String encoded = matcher.group(2);
        String value;

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
        return val.replaceAll(NON_LATIN1_REGEXP.pattern(), "?");
    }

    public record ParseResult(String type, Map<String, String> params) {}

    public static ParseResult parse(String string) {
        if (string == null || string.isEmpty()) {
            throw new IllegalArgumentException("argument string is required");
        }

        Matcher matcher = DISPOSITION_TYPE_REGEXP.matcher(string);

        if (!matcher.find()) {
            throw new IllegalArgumentException("invalid type format");
        }

        // normalize type
        int index = matcher.end() - 1;
        String type = matcher.group(1).toLowerCase();

        String key;
        List<String> names = new ArrayList<>();
        Map<String, String> params = new HashMap<>();
        String value;

        // calculate index to start at
        matcher = PARAM_REGEXP.matcher(string);
        matcher.region(index, string.length());

        // match parameters
        while (matcher.find()) {
            if (matcher.start() != index) {
                continue;
            }

            index = matcher.end();
            key = matcher.group(1).toLowerCase();
            value = matcher.group(2);

            if (names.contains(key)) {
                continue;
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
                    .replaceAll(QESC_REGEXP.pattern(), "$1");
            }

            params.put(key, value);
        }

        return new ParseResult(type, params);
    }

    private static String pDecode(String hex) {
        return String.valueOf((char) Integer.parseInt(hex, 16));
    }

    private static String replaceAll(String input, Replacer replacer) {
        Matcher matcher = HEX_ESCAPE_REPLACE_REGEXP.matcher(input);
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
