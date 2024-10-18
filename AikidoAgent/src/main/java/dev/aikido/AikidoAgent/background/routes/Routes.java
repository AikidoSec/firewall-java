package dev.aikido.AikidoAgent.background.routes;
import dev.aikido.AikidoAgent.context.RouteMetadata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static dev.aikido.AikidoAgent.background.routes.RouteToKeyHelper.routeToKey;

public class Routes {
    private final int maxSize;
    private final Map<String, RouteEntry> routes;

    public Routes(int maxSize) {
        this.maxSize = maxSize;
        this.routes = new HashMap<>();
    }

    public Routes() {
        this(1000); // Default max size
    }

    public void initializeRoute(RouteMetadata routeMetadata) {
        manageRoutesSize();
        String key = routeToKey(routeMetadata);
        if (routes.containsKey(key)) {
            return;
        }
        routes.put(key, new RouteEntry(routeMetadata));
    }

    public void incrementRoute(RouteMetadata routeMetadata) {
        String key = routeToKey(routeMetadata);
        RouteEntry route = routes.get(key);
        if (route != null) {
            route.incrementHits();
        }
    }

    public RouteEntry get(RouteMetadata routeMetadata) {
        String key = routeToKey(routeMetadata);
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

    public Collection<RouteEntry> asList() {
        return routes.values();
    }

    public int size() {
        return routes.size();
    }
}
