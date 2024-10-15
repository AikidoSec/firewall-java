package dev.aikido.AikidoAgent.helpers.url;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlParser {

    public static URI tryParseUrl(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public static String tryParseUrlPath(String url) {
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
