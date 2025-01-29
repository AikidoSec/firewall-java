package storage;

import dev.aikido.agent_api.context.RouteMetadata;
import dev.aikido.agent_api.storage.routes.Routes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static dev.aikido.agent_api.storage.routes.RouteToKeyHelper.routeToKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


public class RoutesTest {
    private Routes routes;

    @BeforeEach
    public void setUp() {
        routes = new Routes();
    }

    @Test
    public void testGetDeltaMapEmpty() {
        // Test when there are no routes initialized
        Map<String, Integer> deltaMap = routes.getDeltaMap();
        assertEquals(0, deltaMap.size());
    }

    @Test
    public void testGetDeltaMapSingleEntry() {
        // Test with a single route
        RouteMetadata routeMetadata = new RouteMetadata("/test", "http://localhost/test", "GET");
        routes.initializeRoute(routeMetadata);
        routes.incrementRoute(routeMetadata);

        Map<String, Integer> deltaMap = routes.getDeltaMap();
        assertEquals(1, deltaMap.size());
        assertEquals(Integer.valueOf(1), deltaMap.get(routeToKey(routeMetadata)));
    }

    @Test
    public void testGetDeltaMapMultipleEntries() {
        // Test with multiple routes
        RouteMetadata routeMetadata1 = new RouteMetadata("/test1", "http://localhost/test1", "GET");
        RouteMetadata routeMetadata2 = new RouteMetadata("/test2", "http://localhost/test2", "POST");
        routes.initializeRoute(routeMetadata1);
        routes.initializeRoute(routeMetadata2);

        routes.incrementRoute(routeMetadata1);
        routes.incrementRoute(routeMetadata1);
        routes.incrementRoute(routeMetadata2);

        Map<String, Integer> deltaMap = routes.getDeltaMap();
        assertEquals(2, deltaMap.size());
        assertEquals(Integer.valueOf(2), deltaMap.get(routeToKey(routeMetadata1)));
        assertEquals(Integer.valueOf(1), deltaMap.get(routeToKey(routeMetadata2)));
    }

    @Test
    public void testGetDeltaMapAfterClearing() {
        // Test after clearing routes
        RouteMetadata routeMetadata = new RouteMetadata("/test", "http://localhost/test", "GET");
        routes.initializeRoute(routeMetadata);
        routes.incrementRoute(routeMetadata);

        routes.clear();

        Map<String, Integer> deltaMap = routes.getDeltaMap();
        assertEquals(0, deltaMap.size());
    }

    @Test
    public void testGetDeltaMapWithNoIncrements() {
        RouteMetadata routeMetadata = new RouteMetadata("/test", "http://localhost/test", "GET");
        routes.initializeRoute(routeMetadata);

        Map<String, Integer> deltaMap = routes.getDeltaMap();
        assertEquals(1, deltaMap.size());
        assertEquals(0, deltaMap.get(routeToKey(routeMetadata)));
    }

    @Test
    public void testImportFromDeltaMap() {
        RouteMetadata routeMetadata1 = new RouteMetadata("/test1", "http://localhost/test1", "GET");
        RouteMetadata routeMetadata2 = new RouteMetadata("/test2", "http://localhost/test2", "POST");
        routes.initializeRoute(routeMetadata1);
        routes.initializeRoute(routeMetadata2);

        Map<String, Integer> deltaMap = new HashMap<>();
        deltaMap.put(routeToKey(routeMetadata1), 3);
        deltaMap.put(routeToKey(routeMetadata2), 2);

        routes.importFromDeltaMap(deltaMap);

        assertEquals(3, routes.get(routeMetadata1).getHits());
        assertEquals(2, routes.get(routeMetadata2).getHits());
    }

    @Test
    public void testImportFromDeltaMapWithNonExistentRoute() {
        RouteMetadata routeMetadata = new RouteMetadata("/test", "http://localhost/test", "GET");
        Map<String, Integer> deltaMap = new HashMap<>();
        deltaMap.put(routeToKey(routeMetadata), 5);

        routes.importFromDeltaMap(deltaMap);

        // Since the route was never initialized, hits should be 0
        assertNull(routes.get(routeMetadata));
    }

    @Test
    public void testImportFromDeltaMapWithZeroHits() {
        RouteMetadata routeMetadata = new RouteMetadata("/test", "http://localhost/test", "GET");
        routes.initializeRoute(routeMetadata);

        Map<String, Integer> deltaMap = new HashMap<>();
        deltaMap.put(routeToKey(routeMetadata), 0); // Importing zero hits

        routes.importFromDeltaMap(deltaMap);

        // The hits should remain the same
        assertEquals(0, routes.get(routeMetadata).getHits());
    }

    @Test
    public void testImportFromDeltaMapWithNegativeHits() {
        RouteMetadata routeMetadata = new RouteMetadata("/test", "http://localhost/test", "GET");
        routes.initializeRoute(routeMetadata);
        routes.incrementRoute(routeMetadata); // Increment once

        Map<String, Integer> deltaMap = new HashMap<>();
        deltaMap.put(routeToKey(routeMetadata), -1); // Importing negative hits

        routes.importFromDeltaMap(deltaMap);

        // The hits should not go below zero
        assertEquals(0, routes.get(routeMetadata).getHits());
    }

    @Test
    public void testGetDeltaMapAfterMultipleIncrements() {
        RouteMetadata routeMetadata = new RouteMetadata("/test", "http://localhost/test", "GET");
        routes.initializeRoute(routeMetadata);

        // Increment hits multiple times
        for (int i = 0; i < 5; i++) {
            routes.incrementRoute(routeMetadata);
        }

        Map<String, Integer> deltaMap = routes.getDeltaMap();
        assertEquals(1, deltaMap.size());
        assertEquals(5, deltaMap.get(routeToKey(routeMetadata)));
    }

    @Test
    public void testGetDeltaMapWithMultipleIncrementsAndClears() {
        RouteMetadata routeMetadata1 = new RouteMetadata("/test1", "http://localhost/test1", "GET");
        RouteMetadata routeMetadata2 = new RouteMetadata("/test2", "http://localhost/test2", "POST");
        routes.initializeRoute(routeMetadata1);
        routes.initializeRoute(routeMetadata2);

        // Increment hits for both routes
        routes.incrementRoute(routeMetadata1);
        routes.incrementRoute(routeMetadata1);
        routes.incrementRoute(routeMetadata2);

        // Clear the routes
        routes.clear();

        // Check delta map after clearing
        Map<String, Integer> deltaMap = routes.getDeltaMap();
        assertEquals(0, deltaMap.size());
    }
}
