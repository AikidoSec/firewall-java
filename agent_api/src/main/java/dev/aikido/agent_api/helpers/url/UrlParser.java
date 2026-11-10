package dev.aikido.agent_api.helpers.url;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Pattern;

public final class UrlParser {
    private UrlParser() {}

    private static final Pattern DUPLICATE_SLASHES = Pattern.compile("/{2,}");

    public static URI tryParseUrl(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    /**
     * Returns the path of a URL the way routers (Spring MVC, Spring WebFlux) read it before matching a handler:
     * path parameters stripped, percent-decoded and duplicate slashes merged, so that e.g. /admin;x=y/delete
     * is seen as /admin/delete. Dot segments are kept, because those routers match them literally;
     * see {@link #resolveDotSegments(String)} for the servlet container's view.
     * <p>
     * This deliberately does not use java.net.URI: servers accept and route URLs that it rejects
     * (e.g. /admin?q=%zz or /files/a|b), and we must still be able to match an endpoint for those.
     */
    public static String tryParseUrlPath(String url) {
        String rawPath = extractRawPath(url);
        if (rawPath == null) {
            return null;
        }
        StringBuilder path = new StringBuilder();
        for (String segment : rawPath.split("/", -1)) {
            int pathParametersStart = segment.indexOf(';');
            if (pathParametersStart >= 0) {
                segment = segment.substring(0, pathParametersStart);
            }
            path.append('/').append(percentDecode(segment));
        }
        return DUPLICATE_SLASHES.matcher(path).replaceAll("/");
    }

    /**
     * Resolves "." and ".." segments like servlet containers do before they pick a servlet,
     * e.g. /public/../admin/delete becomes /admin/delete.
     */
    public static String resolveDotSegments(String path) {
        String[] segments = path.split("/", -1);
        Deque<String> resolved = new ArrayDeque<>();
        for (String segment : segments) {
            if (segment.isEmpty() || segment.equals(".")) {
                continue;
            }
            if (segment.equals("..")) {
                resolved.pollLast();
                continue;
            }
            resolved.addLast(segment);
        }
        if (resolved.isEmpty()) {
            return "/";
        }
        String last = segments[segments.length - 1];
        boolean trailingSlash = last.isEmpty() || last.equals(".") || last.equals("..");
        return "/" + String.join("/", resolved) + (trailingSlash ? "/" : "");
    }

    public static boolean hasDotSegments(String path) {
        for (String segment : path.split("/")) {
            if (segment.equals(".") || segment.equals("..")) {
                return true;
            }
        }
        return false;
    }

    /** Returns the still-encoded path of a relative ("/a?b") or absolute ("http://host/a?b") URL. */
    private static String extractRawPath(String url) {
        if (url == null) {
            return null;
        }
        // Cut the query and fragment first: they may contain slashes that are not part of the path.
        String withoutQuery = url.split("[?#]", 2)[0];
        if (withoutQuery.startsWith("/")) {
            return withoutQuery;
        }
        String[] schemeAndRest = withoutQuery.split("://", 2);
        if (schemeAndRest.length < 2 || schemeAndRest[0].isEmpty()) {
            return null;
        }
        String hostAndPath = schemeAndRest[1];
        int pathStart = hostAndPath.indexOf('/');
        return pathStart < 0 ? "/" : hostAndPath.substring(pathStart);
    }

    private static String percentDecode(String segment) {
        try {
            // URLDecoder follows form encoding where '+' means a space, in a path it's a literal '+'.
            return URLDecoder.decode(segment.replace("+", "%2B"), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            // Malformed escape (e.g. "%zz"): keep the segment as it was sent.
            return segment;
        }
    }
}
