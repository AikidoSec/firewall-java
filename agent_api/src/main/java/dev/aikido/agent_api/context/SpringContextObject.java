package dev.aikido.agent_api.context;

import java.util.HashMap;
import java.util.Map;

public class SpringContextObject extends ContextObject {
    // We use this map for when @RequestBody does not get used :
    protected transient Map<String, Object> bodyMap = new HashMap<>();

    // We don't have access yet to the route parameters: doFilter() is called before the Controller
    // So the parameters will be set later.
    protected transient Map<String, Object> params = new HashMap<>();

    public void setParameter(String key, String value) {
        this.params.put(key, value);
        this.cache.remove("routeParams"); // Reset cache
    }

    public void setBodyElement(String key, Object value) {
        bodyMap.put(key, value);
        cache.remove("body"); // Reset body cache.
    }
}

