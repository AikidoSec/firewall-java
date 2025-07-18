package collectors;

import dev.aikido.agent_api.collectors.WebResponseCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.RouteMetadata;
import dev.aikido.agent_api.storage.routes.RoutesStore;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;


public class WebResponseCollectorTest {
    public static class SampleContextObject extends ContextObject {
        public SampleContextObject() {
            this("GET");
        }
        public SampleContextObject(String method) {
            this.method = method;
            this.source = "web";
            this.url = "https://example.com/api/resource";
            this.route = "/api/resource";
            this.remoteAddress = "192.168.1.1";
            this.headers = new HashMap<>();
            this.query = new HashMap<>();
            this.cookies = new HashMap<>();
            this.body = "{\"key\":\"value\"}"; // Body as a JSON string
            this.executedMiddleware = true; // Start with "executed middleware" as true
        }
    }

    @BeforeAll
    public static void clean() {
        Context.set(null);
    };

    @AfterEach
    public void tearDown() throws SQLException {
        Context.set(null);
        RoutesStore.clear();
    }

    @Test
    public void testResponseCollector1() throws SQLException {
        // Test new route :
        Context.set(new SampleContextObject());

        assertEquals(0, RoutesStore.getRoutesAsList().length);
        WebResponseCollector.report(200);
        assertEquals(1, RoutesStore.getRoutesAsList().length);
        assertEquals(1, RoutesStore.getRouteHits("GET", "/api/resource"));

        // Test same route but incremented hits :
        WebResponseCollector.report(201);
        assertEquals(1, RoutesStore.getRoutesAsList().length);
        assertEquals(2, RoutesStore.getRouteHits("GET", "/api/resource"));

        // Test same route but invalid status code
        WebResponseCollector.report(0);
        assertEquals(1, RoutesStore.getRoutesAsList().length);
        assertEquals(2, RoutesStore.getRouteHits("GET", "/api/resource"));

        // Test same route but context not set :
        Context.set(null);
        WebResponseCollector.report(200);
        assertEquals(1, RoutesStore.getRoutesAsList().length);
        assertEquals(2, RoutesStore.getRouteHits("GET", "/api/resource"));

        RoutesStore.clear();
        assertEquals(0, RoutesStore.getRoutesAsList().length);
    }


    @Test
    public void testResponseCollectorWithInvalidMethodOrStatusCode() throws SQLException {
        // Test with invalid method :
        Context.set(new SampleContextObject("OPTIONS"));
        assertEquals(0, RoutesStore.getRoutesAsList().length);
        WebResponseCollector.report(200);
        assertEquals(0, RoutesStore.getRoutesAsList().length);

        Context.set(new SampleContextObject());
        WebResponseCollector.report(400);
        assertEquals(0, RoutesStore.getRoutesAsList().length);
        WebResponseCollector.report(199);
        assertEquals(0, RoutesStore.getRoutesAsList().length);
        WebResponseCollector.report(-200);
        assertEquals(0, RoutesStore.getRoutesAsList().length);
    }
}

