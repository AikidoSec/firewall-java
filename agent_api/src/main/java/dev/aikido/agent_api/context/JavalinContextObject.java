package dev.aikido.agent_api.context;

import java.util.*;

import static dev.aikido.agent_api.helpers.net.ProxyForwardedParser.getIpFromRequest;
import static dev.aikido.agent_api.helpers.url.BuildRouteFromUrl.buildRouteFromUrl;

public class JavalinContextObject extends ContextObject {
    // We use this map for when @RequestBody does not get used :
    protected transient Map<String, Object> bodyMap = new HashMap<>();
    public JavalinContextObject(
            String method, String url, String rawIp, Map<String, List<String>> queryParams,
            Map<String, String> cookies, Map<String, String> headers
    ) {
        this.method = method;
        if (url != null) {
            this.url = url.toString();
        }
        this.query = new HashMap<>(queryParams);
        this.cookies = extractCookies(cookies);
        this.headers = new HashMap<>(headers);
        this.route = buildRouteFromUrl(this.url);
        this.remoteAddress = getIpFromRequest(rawIp, this.headers);
        this.source = "SpringFramework";
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
}
