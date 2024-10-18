package background;

import dev.aikido.AikidoAgent.background.routes.RouteEntry;
import dev.aikido.AikidoAgent.background.routes.Routes;
import dev.aikido.AikidoAgent.context.RouteMetadata;
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
        routeMetadata1 = new RouteMetadata("GET", "", "/api/test1");
        routeMetadata2 = new RouteMetadata("POST", "", "/api/test2");
        routeMetadata3 = new RouteMetadata("PUT", "", "/api/test3");
    }

    @Test
    void testInitializeRoute() {
        routes.initializeRoute(routeMetadata1);
        assertEquals(1, routes.size());
        assertNotNull(routes.get(routeMetadata1));
    }

    @Test
    void testInitializeDuplicateRoute() {
        routes.initializeRoute(routeMetadata1);
        routes.initializeRoute(routeMetadata1); // Should not add again
        assertEquals(1, routes.size());
    }

    @Test
    void testIncrementRouteHits() {
        routes.initializeRoute(routeMetadata1);
        routes.incrementRoute(routeMetadata1);
        RouteEntry entry = routes.get(routeMetadata1);
        assertNotNull(entry);
        assertEquals(1, entry.getHits());
    }

    @Test
    void testIncrementNonExistentRoute() {
        routes.incrementRoute(routeMetadata1); // Should not throw or add
        assertEquals(0, routes.size());
    }

    @Test
    void testManageRoutesSize() {
        routes.initializeRoute(routeMetadata1);
        routes.initializeRoute(routeMetadata2);
        assertEquals(2, routes.size());

        routes.initializeRoute(routeMetadata3); // This should evict the least used route

        assertEquals(2, routes.size());
        assertNull(routes.get(routeMetadata1)); // routeMetadata1 should be evicted
        assertNotNull(routes.get(routeMetadata2)); // routeMetadata2 should still exist
    }

    @Test
    void testClearRoutes() {
        routes.initializeRoute(routeMetadata1);
        routes.clear();
        assertEquals(0, routes.size());
    }

    @Test
    void testMultipleInitializations() {
        routes.initializeRoute(routeMetadata1);
        routes.initializeRoute(routeMetadata2);
        routes.initializeRoute(routeMetadata3);
        assertEquals(2, routes.size()); // Only 2 should remain
    }

    @Test
    void testIncrementMultipleTimes() {
        routes.initializeRoute(routeMetadata1);
        for (int i = 0; i < 5; i++) {
            routes.incrementRoute(routeMetadata1);
        }
        RouteEntry entry = routes.get(routeMetadata1);
        assertNotNull(entry);
        assertEquals(5, entry.getHits());
    }

    @Test
    void testEvictionOrder() {
        routes.initializeRoute(routeMetadata1);
        routes.incrementRoute(routeMetadata1); // Increment hits for routeMetadata1
        routes.initializeRoute(routeMetadata2);
        routes.initializeRoute(routeMetadata3); // This should evict routeMetadata2 (routeMetadata1 has more hits)

        assertNull(routes.get(routeMetadata2)); // routeMetadata2 should be evicted
        assertNotNull(routes.get(routeMetadata1)); // routeMetadata1 should still exist
        assertNotNull(routes.get(routeMetadata3)); // routeMetadata3 should exist
    }

    @Test
    void testSizeAfterEviction() {
        routes.initializeRoute(routeMetadata1);
        routes.initializeRoute(routeMetadata2);
        assertEquals(2, routes.size());

        routes.initializeRoute(new RouteMetadata("DELETE", "", "/api/test4")); // Evict one
        assertEquals(2, routes.size()); // Size should remain 2
    }

    @Test
    void testIterator() {
        routes.initializeRoute(routeMetadata1);
        routes.initializeRoute(routeMetadata2);
        assertEquals(2, routes.size());
    }

    @Test
    void testIteratorAfterEviction() {
        routes.initializeRoute(routeMetadata1);
        routes.initializeRoute(routeMetadata2);
        assertEquals(2, routes.size()); // Should still be 2 after eviction
        routes.initializeRoute(routeMetadata3); // Evict one

        assertEquals(2, routes.size()); // Should still be 2 after eviction
    }
}
