package collectors;

import dev.aikido.agent_api.collectors.WebResponseCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.RouteMetadata;
import dev.aikido.agent_api.storage.routes.Routes;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static utils.EmtpyThreadCacheObject.getEmptyThreadCacheObject;

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
    public static RouteMetadata routeMetadata1 = new RouteMetadata("/api/resource", "https://example.com/api/resource", "GET");

    @BeforeAll
    public static void clean() {
        Context.set(null);
        ThreadCache.set(null);
    };
    @BeforeEach
    public void setUp() throws SQLException {
        // Connect to the MySQL database
        ThreadCache.set(getEmptyThreadCacheObject());
    }

    @AfterEach
    public void tearDown() throws SQLException {
        Context.set(null);
        ThreadCache.set(null);
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    public void testResponseCollector1() throws SQLException {
        // Test new route :
        Context.set(new SampleContextObject());

        assertEquals(0, ThreadCache.get().getRoutes().size());
        WebResponseCollector.report(200);
        assertEquals(1, ThreadCache.get().getRoutes().size());
        assertEquals(1, ThreadCache.get().getRoutes().get(routeMetadata1).getHits());

        // Test same route but incremented hits :
        WebResponseCollector.report(201);
        assertEquals(1, ThreadCache.get().getRoutes().size());
        assertEquals(2, ThreadCache.get().getRoutes().get(routeMetadata1).getHits());

        // Test same route but invalid status code
        WebResponseCollector.report(0);
        assertEquals(1, ThreadCache.get().getRoutes().size());
        assertEquals(2, ThreadCache.get().getRoutes().get(routeMetadata1).getHits());

        // Test same route but context not set :
        Context.set(null);
        WebResponseCollector.report(200);
        assertEquals(1, ThreadCache.get().getRoutes().size());
        assertEquals(2, ThreadCache.get().getRoutes().get(routeMetadata1).getHits());
    }


    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    public void testResponseCollectorWithInvalidMethodOrStatusCode() throws SQLException {
        // Test with invalid method :
        Context.set(new SampleContextObject("OPTIONS"));
        assertEquals(0, ThreadCache.get().getRoutes().size());
        WebResponseCollector.report(200);
        assertEquals(0, ThreadCache.get().getRoutes().size());

        Context.set(new SampleContextObject());
        WebResponseCollector.report(400);
        assertEquals(0, ThreadCache.get().getRoutes().size());
        WebResponseCollector.report(199);
        assertEquals(0, ThreadCache.get().getRoutes().size());
        WebResponseCollector.report(-200);
        assertEquals(0, ThreadCache.get().getRoutes().size());
    }
    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    public void testNothingHappensWithEmptyThreadCache() throws SQLException {
        // Test with emtpy thread cache :
        Context.set(new SampleContextObject());
        ThreadCacheObject threadCacheObject = ThreadCache.get();
        ThreadCache.set(null);
        assertEquals(0, threadCacheObject.getRoutes().size());
        WebResponseCollector.report(200);
        assertEquals(0, threadCacheObject.getRoutes().size());

        // Test with emtpy thread cache getRoutes() :
        Context.set(new SampleContextObject());
        ThreadCache.set(new ThreadCacheObject(null, null, null, null, Optional.empty()));
        assertNull(ThreadCache.get().getRoutes());
        WebResponseCollector.report(200);
        assertNull(ThreadCache.get().getRoutes());
    }


}
