package dev.aikido.agent_api.storage.routes;

import dev.aikido.agent_api.api_discovery.APISpec;
import dev.aikido.agent_api.context.RouteMetadata;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import java.util.concurrent.locks.ReentrantLock;

public final class RoutesStore {
    private static final Logger logger = LogManager.getLogger(RoutesStore.class);
    private static final ReentrantLock mutex = new ReentrantLock();
    private static final Routes routes = new Routes();

    private RoutesStore() {

    }

    public static Routes getRoutes() {
        mutex.lock();
        Routes routes = RoutesStore.routes;
        mutex.unlock();
        return routes;
    }

    public static void updateApiSpec(RouteMetadata routeMetadata, APISpec apiSpec) {
        mutex.lock();
        try {
            RouteEntry route = routes.get(routeMetadata);
            if (route != null) {
                route.updateApiSpec(apiSpec);
            }
        } catch (Throwable e) {
            logger.debug("Error occurred updating api specs: %s", e.getMessage());
        }
        mutex.unlock();
    }

    public static void addRouteHits(RouteMetadata routeMetadata) {
        mutex.lock();
        try {
            routes.incrementRoute(routeMetadata);
        } catch (Throwable e) {
            logger.debug("Error occurred incrementing route hits: %s", e.getMessage());
        }
        mutex.unlock();
    }
}
