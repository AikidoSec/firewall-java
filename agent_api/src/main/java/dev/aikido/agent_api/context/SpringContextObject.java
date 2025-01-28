package dev.aikido.agent_api.context;

import java.util.*;

import static dev.aikido.agent_api.helpers.net.ProxyForwardedParser.getIpFromRequest;
import static dev.aikido.agent_api.helpers.url.BuildRouteFromUrl.buildRouteFromUrl;

public class SpringContextObject extends ContextObject {
    // We use this map for when @RequestBody does not get used :
    protected transient Map<String, Object> bodyMap = new HashMap<>();
    public SpringContextObject(
            String method, StringBuffer url, String rawIp, Map<String, String[]> queryParams,
            HashMap<String, List<String>> cookies, HashMap<String, String> headers
    ) {
        this.method = method;
        if (url != null) {
            this.url = url.toString();
        }
        this.query = extractQueryParameters(queryParams);
        this.cookies = cookies;
        this.headers = headers;
        this.route = buildRouteFromUrl(this.url);
        this.remoteAddress = getIpFromRequest(rawIp, this.headers);
        this.source = "SpringFramework";
        this.redirectStartNodes = new ArrayList<>();

        // We don't have access yet to the route parameters: doFilter() is called before the Controller
        // So the parameters will be set later.
        this.params = null;
    }
    @Override
    public Object getBody() {
        if (this.body != null) {
            // @RequestBody was used, all data is available :
            return this.body;
        }
        return this.bodyMap; // Use the selected fields that were extracted.
    }
    public void setBodyElement(String key, Object value) {
        bodyMap.put(key, value);
        cache.remove("body"); // Reset body cache.
    }
    public void setParams(Object params) {
        this.params = params;
        this.cache.remove("routeParams"); // Reset cache
    }

    private static HashMap<String, List<String>> extractQueryParameters(Map<String, String[]> parameterMap) {
        HashMap<String, List<String>> query = new HashMap<>();

        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            // Convert String[] to List<String>
            List<String> list = Arrays.asList(entry.getValue());
            query.put(entry.getKey(), list);
        }
        return query;
    }
}
