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

    public static int getRouteHits(String method, String route) {
        mutex.lock();
        try {
            return routes.get(method, route).getHits();
        } finally {
            mutex.unlock();
        }
    }

    public static RouteEntry[] getRoutesAsList() {
        mutex.lock();
        try {
            return routes.asList();
        } finally {
            mutex.unlock();
        }
    }

    public static void updateApiSpec(String method, String route, APISpec apiSpec) {
        mutex.lock();
        try {
            RouteEntry routeEntry = routes.get(method, route);
            if (routeEntry != null) {
                routeEntry.updateApiSpec(apiSpec);
            }
        } catch (Throwable e) {
            logger.debug("Error occurred updating api specs: %s", e.getMessage());
        } finally {
            mutex.unlock();
        }
    }

    public static void addRouteHits(String method, String route) {
        mutex.lock();
        try {
            routes.incrementRoute(method, route);
        } catch (Throwable e) {
            logger.debug("Error occurred incrementing route hits: %s", e.getMessage());
        } finally {
            mutex.unlock();
        }
    }

    public static void addRouteRateLimitedCount(String method, String route) {
        mutex.lock();
        try {
            routes.incrementRateLimitCount(method, route);
        } catch (Throwable e) {
            logger.debug("Error occurred incrementing route rate limit count: %s", e.getMessage());
        } finally {
            mutex.unlock();
        }
    }

    public static void clear() {
        mutex.lock();
        try {
            routes.clear();
        } catch (Throwable e) {
            logger.debug("Error occurred whilst clearing routes: %s", e.getMessage());
        } finally {
            mutex.unlock();
        }
    }
}
