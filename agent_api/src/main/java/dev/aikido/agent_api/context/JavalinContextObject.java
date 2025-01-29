package dev.aikido.agent_api.context;

import static dev.aikido.agent_api.helpers.net.ProxyForwardedParser.getIpFromRequest;
import static dev.aikido.agent_api.helpers.url.BuildRouteFromUrl.buildRouteFromUrl;

import java.util.*;

public class JavalinContextObject extends ContextObject {
    // We use this map for when @RequestBody does not get used :
    protected transient Map<String, Object> bodyMap = new HashMap<>();

    public JavalinContextObject(
            String method,
            String url,
            String rawIp,
            Map<String, List<String>> queryParams,
            Map<String, String> cookies,
            Map<String, String> headers) {
        this.method = method;
        if (url != null) {
            this.url = url.toString();
        }
        this.query = new HashMap<>(queryParams);
        this.cookies = extractCookies(cookies);
        this.headers = extractHeaders(headers);
        this.route = buildRouteFromUrl(this.url);
        this.remoteAddress = getIpFromRequest(rawIp, this.headers);
        this.source = "Javalin";
        this.redirectStartNodes = new ArrayList<>();

        // We don't have access yet to the route parameters, will add once we have access.
        this.params = null;
    }

    public void setParams(Object params) {
        this.params = params;
        this.cache.remove("routeParams"); // Reset cache
    }

    private static HashMap<String, List<String>> extractCookies(Map<String, String> cookieMap) {
        HashMap<String, List<String>> cookies = new HashMap<>();

        for (Map.Entry<String, String> entry : cookieMap.entrySet()) {
            cookies.put(entry.getKey(), List.of(entry.getValue()));
        }
        return cookies;
    }

    private static HashMap<String, String> extractHeaders(Map<String, String> rawHeaders) {
        HashMap<String, String> headers = new HashMap<>();
        for (Map.Entry<String, String> entry : rawHeaders.entrySet()) {
            // Lower-case keys :
            headers.put(entry.getKey().toLowerCase(), entry.getValue());
        }
        return headers;
    }
}
