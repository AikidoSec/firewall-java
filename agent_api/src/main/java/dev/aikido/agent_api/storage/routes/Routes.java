package dev.aikido.agent_api.storage.routes;
import dev.aikido.agent_api.context.RouteMetadata;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static dev.aikido.agent_api.storage.routes.RouteToKeyHelper.routeToKey;

public class Routes {
    private final int maxSize;
    private final Map<String, RouteEntry> routes;

    public Routes(int maxSize) {
        this.maxSize = maxSize;
        this.routes = new LinkedHashMap<>();
    }
    public Routes() {
        this(1000); // Default max size
    }

    private void ensureRoute(String method, String route) {
        manageRoutesSize();
        String key = routeToKey(method, route);
        if(!routes.containsKey(key)) {
            routes.put(key, new RouteEntry(method, route));
        }
    }

    public void incrementRoute(String method, String route) {
        ensureRoute(method, route);
        RouteEntry routeEntry = this.get(method, route);
        if (routeEntry != null) {
            routeEntry.incrementHits();
        }
    }

    public void incrementRateLimitCount(String method, String route) {
        ensureRoute(method, route);
        RouteEntry routeEntry = this.get(method, route);
        if (routeEntry != null) {
            routeEntry.incrementRateLimitCount();
        }
    }

    public RouteEntry get(String method, String route) {
        String key = routeToKey(method, route);
        return routes.get(key);
    }

    public void clear() {
        routes.clear();
    }

    private void manageRoutesSize() {
        if (routes.size() >= maxSize) {
            String leastUsedKey = null;
            int leastHits = Integer.MAX_VALUE;

            for (Map.Entry<String, RouteEntry> entry : routes.entrySet()) {
                if (entry.getValue().getHits() < leastHits) {
                    leastUsedKey = entry.getKey();
                    leastHits = entry.getValue().getHits();
                }
            }

            if (leastUsedKey != null) {
                routes.remove(leastUsedKey);
            }
        }
    }

    public RouteEntry[] asList() {
        return routes.values().toArray(new RouteEntry[0]);
    }

    public int size() {
        return routes.size();
    }
}
