package storage;

import dev.aikido.agent_api.storage.routes.RouteEntry;
import dev.aikido.agent_api.storage.routes.Routes;
import dev.aikido.agent_api.context.RouteMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoutesTest {

    private Routes routes;
    private RouteMetadata routeMetadata1;
    private RouteMetadata routeMetadata2;
    private RouteMetadata routeMetadata3;

    @BeforeEach
    void setUp() {
        routes = new Routes(2); // Set max size to 2 for testing
        routeMetadata1 = new RouteMetadata("/api/test1", "/api/test1", "GET");
        routeMetadata2 = new RouteMetadata( "/api/test2", "/api/test2", "POST");
        routeMetadata3 = new RouteMetadata("/api/test3", "/api/test3", "PUT");
    }

    @Test
    void testInitializeRoute() {
        routes.incrementRoute(routeMetadata1);
        assertEquals(1, routes.size());
        assertNotNull(routes.get(routeMetadata1));
    }

    @Test
    void testInitializeDuplicateRoute() {
        routes.incrementRoute(routeMetadata1);
        routes.incrementRoute(routeMetadata1); // Should not add again
        assertEquals(1, routes.size());
    }

    @Test
    void testIncrementRouteHits() {
        routes.incrementRoute(routeMetadata1);
        RouteEntry entry = routes.get(routeMetadata1);
        assertNotNull(entry);
        assertEquals(1, entry.getHits());
    }

    @Test
    void testIncrementNonExistentRoute() {
        routes.incrementRoute(routeMetadata1);
        assertEquals(1, routes.size());
    }

    @Test
    void testIncrementRouteRateLimitCount() {
        routes.incrementRateLimitCount(routeMetadata1);
        RouteEntry entry = routes.get(routeMetadata1);
        assertNotNull(entry);
        assertEquals(1, entry.getRateLimitCount());
    }

    @Test
    void testIncrementNonExistentRouteRateLimit() {
        routes.incrementRateLimitCount(routeMetadata1);
        assertEquals(1, routes.size());
    }

    @Test
    void testManageRoutesSize() {
        routes.incrementRoute(routeMetadata1);
        routes.incrementRoute(routeMetadata2);
        assertEquals(2, routes.size());

        routes.incrementRoute(routeMetadata3); // This should evict the least used route

        assertEquals(2, routes.size());
        assertNull(routes.get(routeMetadata1)); // routeMetadata1 should be evicted
        assertNotNull(routes.get(routeMetadata2)); // routeMetadata2 should still exist
    }

    @Test
    void testClearRoutes() {
        routes.incrementRoute(routeMetadata1);
        routes.clear();
        assertEquals(0, routes.size());
    }

    @Test
    void testMultipleInitializations() {
        routes.incrementRoute(routeMetadata1);
        routes.incrementRoute(routeMetadata2);
        routes.incrementRoute(routeMetadata3);
        assertEquals(2, routes.size()); // Only 2 should remain
    }

    @Test
    void testIncrementMultipleTimes() {
        routes.incrementRoute(routeMetadata1);
        for (int i = 0; i < 5; i++) {
            routes.incrementRoute(routeMetadata1);
        }
        RouteEntry entry = routes.get(routeMetadata1);
        assertNotNull(entry);
        assertEquals(6, entry.getHits());
    }

    @Test
    void testDefaultConstructor() {
        routes = new Routes();
        routes.incrementRoute(routeMetadata1);
        routes.incrementRoute(routeMetadata1); // Increment hits for routeMetadata1
        routes.incrementRoute(routeMetadata2);
        for (int i = 0; i < (1000 - 1); i++) {
            routes.incrementRoute(new RouteMetadata(String.valueOf(i), "api/test3", "GET"));
        }
        assertEquals(1000, routes.asList().length);
        assertNull(routes.get(routeMetadata2)); // routeMetadata2 should be evicted
        assertNotNull(routes.get(routeMetadata1)); // routeMetadata1 should still exist
    }

    @Test
    void testEvictionOrder() {
        routes.incrementRoute(routeMetadata1);
        routes.incrementRoute(routeMetadata1); // Increment hits for routeMetadata1
        routes.incrementRoute(routeMetadata2);
        routes.incrementRoute(routeMetadata3); // This should evict routeMetadata2 (routeMetadata1 has more hits)

        assertNull(routes.get(routeMetadata2)); // routeMetadata2 should be evicted
        assertNotNull(routes.get(routeMetadata1)); // routeMetadata1 should still exist
        assertNotNull(routes.get(routeMetadata3)); // routeMetadata3 should exist
    }

    @Test
    void testSizeAfterEviction() {
        routes.incrementRoute(routeMetadata1);
        routes.incrementRoute(routeMetadata2);
        assertEquals(2, routes.size());

        routes.incrementRoute(new RouteMetadata("DELETE", "", "/api/test4")); // Evict one
        assertEquals(2, routes.size()); // Size should remain 2
    }

    @Test
    void testIterator() {
        routes.incrementRoute(routeMetadata1);
        routes.incrementRoute(routeMetadata2);
        assertEquals(2, routes.size());
    }

    @Test
    void testIteratorAfterEviction() {
        routes.incrementRoute(routeMetadata1);
        routes.incrementRoute(routeMetadata2);
        assertEquals(2, routes.size()); // Should still be 2 after eviction
        routes.incrementRoute(routeMetadata3); // Evict one

        assertEquals(2, routes.size()); // Should still be 2 after eviction
    }
}
