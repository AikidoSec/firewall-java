package dev.aikido.agent_api.context;

import java.util.*;

import static dev.aikido.agent_api.helpers.net.ProxyForwardedParser.getIpFromRequest;
import static dev.aikido.agent_api.helpers.url.BuildRouteFromUrl.buildRouteFromUrl;

public class MicronautContextObject extends ContextObject {
    // Partial body fields (e.g. @QueryValue / @Part) when @Body is not used :
    protected transient Map<String, Object> bodyMap = new HashMap<>();
    // Route/path variables (@PathVariable), set at controller invocation :
    protected transient Map<String, String> routeParams = new HashMap<>();

    public MicronautContextObject(
            String method, String uri, String rawIp,
            Map<String, List<String>> query,
            HashMap<String, List<String>> cookies,
            Map<String, List<String>> headers
    ) {
        this.method = method;
        this.url = uri;
        this.cookies = cookies;
        this.query = new HashMap<>(query);
        this.headers = extractHeaders(headers);
        this.route = buildRouteFromUrl(this.url);
        this.remoteAddress = getIpFromRequest(rawIp, this.headers);
        this.source = "Micronaut";
        this.redirectStartNodes = new ArrayList<>();
    }

    public void setParameter(String key, String value) {
        this.routeParams.put(key, value);
        this.cache.remove("routeParams");
    }

    @Override
    public Object getParams() {
        return routeParams;
    }

    public void setBodyElement(String key, Object value) {
        this.bodyMap.put(key, value);
        this.cache.remove("body");
    }

    @Override
    public Object getBody() {
        if (this.body != null) {
            return this.body; // @Body was used, full data available
        }
        return this.bodyMap; // otherwise the selected fields
    }

    private static HashMap<String, List<String>> extractHeaders(Map<String, List<String>> map) {
        HashMap<String, List<String>> newMap = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            newMap.put(entry.getKey().toLowerCase(), entry.getValue());
        }
        return newMap;
    }
}
