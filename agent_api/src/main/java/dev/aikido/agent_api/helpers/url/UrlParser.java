package dev.aikido.agent_api.helpers.url;

import java.net.URI;
import java.net.URISyntaxException;

public final class UrlParser {
    private UrlParser() {
    }

    public static URI tryParseUrl(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public static String tryParseUrlPath(String url) {
        if (url == null) {
            return null;
        }
        URI parsed = tryParseUrl(url.startsWith("/") ? "http://localhost" + url : url);
        if (parsed == null || parsed.getScheme() == null) {
            return null;
        }
        if (parsed.getPath() == null || parsed.getPath().isEmpty()) {
            return "/";
        }
        return parsed.getPath();
    }

}
