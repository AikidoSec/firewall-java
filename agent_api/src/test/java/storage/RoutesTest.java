package storage;

import dev.aikido.agent_api.storage.routes.RouteEntry;
import dev.aikido.agent_api.storage.routes.Routes;
import dev.aikido.agent_api.context.RouteMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoutesTest {

    private Routes routes;

    @BeforeEach
    void setUp() {
        routes = new Routes(2); // Set max size to 2 for testing
    }

    @Test
    void testInitializeRoute() {
        routes.incrementRoute("GET", "/api/test1");
        assertEquals(1, routes.size());
        assertNotNull(routes.get("GET", "/api/test1"));
    }

    @Test
    void testInitializeDuplicateRoute() {
        routes.incrementRoute("GET", "/api/test1");
        routes.incrementRoute("GET", "/api/test1"); // Should not add again
        assertEquals(1, routes.size());
    }

    @Test
    void testIncrementRouteHits() {
        routes.incrementRoute("GET", "/api/test1");
        RouteEntry entry = routes.get("GET", "/api/test1");
        assertNotNull(entry);
        assertEquals(1, entry.getHits());
    }

    @Test
    void testIncrementNonExistentRoute() {
        routes.incrementRoute("GET", "/api/test1");
        assertEquals(1, routes.size());
    }

    @Test
    void testIncrementRouteRateLimitCount() {
        routes.incrementRateLimitCount("GET", "/api/test1");
        RouteEntry entry = routes.get("GET", "/api/test1");
        assertNotNull(entry);
        assertEquals(1, entry.getRateLimitCount());
    }

    @Test
    void testIncrementNonExistentRouteRateLimit() {
        routes.incrementRateLimitCount("GET", "/api/test1");
        assertEquals(1, routes.size());
    }

    @Test
    void testManageRoutesSize() {
        routes.incrementRoute("GET", "/api/test1");
        routes.incrementRoute("POST", "/api/test2");
        assertEquals(2, routes.size());

        routes.incrementRoute("PUT", "/api/test3"); // This should evict the least used route

        assertEquals(2, routes.size());
        assertNull(routes.get("GET", "/api/test1")); // routeMetadata1 should be evicted
        assertNotNull(routes.get("POST", "/api/test2")); // "POST", "/api/test2" should still exist
    }

    @Test
    void testClearRoutes() {
        routes.incrementRoute("GET", "/api/test1");
        routes.clear();
        assertEquals(0, routes.size());
    }

    @Test
    void testMultipleInitializations() {
        routes.incrementRoute("GET", "/api/test1");
        routes.incrementRoute("POST", "/api/test2");
        routes.incrementRoute("PUT", "/api/test3");
        assertEquals(2, routes.size()); // Only 2 should remain
    }

    @Test
    void testIncrementMultipleTimes() {
        routes.incrementRoute("GET", "/api/test1");
        for (int i = 0; i < 5; i++) {
            routes.incrementRoute("GET", "/api/test1");
        }
        RouteEntry entry = routes.get("GET", "/api/test1");
        assertNotNull(entry);
        assertEquals(6, entry.getHits());
    }

    @Test
    void testDefaultConstructor() {
        routes = new Routes();
        routes.incrementRoute("GET", "/api/test1");
        routes.incrementRoute("GET", "/api/test1"); // Increment hits for routeMetadata1
        routes.incrementRoute("POST", "/api/test2");
        for (int i = 0; i < (1000 - 1); i++) {
            routes.incrementRoute("GET", String.valueOf(i));
        }
        assertEquals(1000, routes.asList().length);
        assertNull(routes.get("POST", "/api/test2")); // "POST", "/api/test2" should be evicted
        assertNotNull(routes.get("GET", "/api/test1")); // routeMetadata1 should still exist
    }

    @Test
    void testEvictionOrder() {
        routes.incrementRoute("GET", "/api/test1");
        routes.incrementRoute("GET", "/api/test1"); // Increment hits for routeMetadata1
        routes.incrementRoute("POST", "/api/test2");
        routes.incrementRoute("PUT", "/api/test3"); // This should evict "POST", "/api/test2" (routeMetadata1 has more hits)

        assertNull(routes.get("POST", "/api/test2")); // "POST", "/api/test2" should be evicted
        assertNotNull(routes.get("GET", "/api/test1")); // routeMetadata1 should still exist
        assertNotNull(routes.get("PUT", "/api/test3")); // "PUT", "/api/test3" should exist
    }

    @Test
    void testSizeAfterEviction() {
        routes.incrementRoute("GET", "/api/test1");
        routes.incrementRoute("POST", "/api/test2");
        assertEquals(2, routes.size());

        routes.incrementRoute("DELETE", "/api/test4");
        assertEquals(2, routes.size()); // Size should remain 2
    }

    @Test
    void testIterator() {
        routes.incrementRoute("GET", "/api/test1");
        routes.incrementRoute("POST", "/api/test2");
        assertEquals(2, routes.size());
    }

    @Test
    void testIteratorAfterEviction() {
        routes.incrementRoute("GET", "/api/test1");
        routes.incrementRoute("POST", "/api/test2");
        assertEquals(2, routes.size()); // Should still be 2 after eviction
        routes.incrementRoute("PUT", "/api/test3"); // Evict one

        assertEquals(2, routes.size()); // Should still be 2 after eviction
    }
}
