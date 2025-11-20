package collectors;

import dev.aikido.agent_api.background.cloud.api.events.DetectedAttackWave;
import dev.aikido.agent_api.collectors.WebRequestCollector;
import dev.aikido.agent_api.collectors.WebResponseCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.RouteMetadata;
import dev.aikido.agent_api.context.User;
import dev.aikido.agent_api.storage.AttackQueue;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import dev.aikido.agent_api.storage.routes.RoutesStore;
import dev.aikido.agent_api.storage.statistics.StatisticsStore;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import utils.EmptySampleContextObject;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static utils.EmptyAPIResponses.emptyAPIListsResponse;
import static utils.EmptyAPIResponses.emptyAPIResponse;


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
    };

    @AfterEach
    public void tearDown() throws SQLException {
        Context.set(null);
        RoutesStore.clear();
        AttackQueue.clear();
        StatisticsStore.clear();
    }

    @Test
    public void testResponseCollector1() throws SQLException {
        // Test new route :
        Context.set(new SampleContextObject());

        assertEquals(0, RoutesStore.getRoutesAsList().length);
        WebResponseCollector.report(200);
        assertEquals(1, RoutesStore.getRoutesAsList().length);
        assertEquals(1, RoutesStore.getRouteHits(routeMetadata1));

        // Test same route but incremented hits :
        WebResponseCollector.report(201);
        assertEquals(1, RoutesStore.getRoutesAsList().length);
        assertEquals(2, RoutesStore.getRouteHits(routeMetadata1));

        // Test same route but invalid status code
        WebResponseCollector.report(0);
        assertEquals(1, RoutesStore.getRoutesAsList().length);
        assertEquals(2, RoutesStore.getRouteHits(routeMetadata1));

        // Test same route but context not set :
        Context.set(null);
        WebResponseCollector.report(200);
        assertEquals(1, RoutesStore.getRoutesAsList().length);
        assertEquals(2, RoutesStore.getRouteHits(routeMetadata1));

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

    @Test
    public void testAttackWaveDetectionWithUserSet() throws SQLException, InterruptedException {
        // Setup
        ServiceConfigStore.updateFromAPIResponse(emptyAPIResponse);
        ServiceConfigStore.updateFromAPIListsResponse(emptyAPIListsResponse);
        
        // Create attack wave context (unusual route/method that triggers attack wave detection)
        ContextObject attackWaveCtx = new EmptySampleContextObject("/wp-config.php", "BADMETHOD", Map.of());
        
        // Set a user in the context (simulating SetUser.setUser being called during request)
        User testUser = new User("user123", "John Doe", "192.168.1.1", System.currentTimeMillis());
        attackWaveCtx.setUser(testUser);
        
        // Simulate the request flow: first WebRequestCollector, then trigger attack wave detection
        // We need to trigger attack waves by making multiple requests
        for (int i = 0; i < 15; i++) {
            Context.set(attackWaveCtx);
            WebRequestCollector.report(attackWaveCtx);
        }
        
        // Now call WebResponseCollector which should detect the attack wave WITH user info
        Context.set(attackWaveCtx);
        WebResponseCollector.report(200);
        
        // Verify attack wave was detected
        assertTrue(AttackQueue.getSize() > 0, "Attack wave should be detected");
        assertEquals(1, StatisticsStore.getStatsRecord().requests().attackWaves().total());
        
        // Get the attack wave event and verify user information is captured
        DetectedAttackWave.DetectedAttackWaveEvent event = 
            (DetectedAttackWave.DetectedAttackWaveEvent) AttackQueue.get();
        
        assertNotNull(event);
        assertEquals("detected_attack_wave", event.type());
        assertEquals("192.168.1.1", event.request().ipAddress());
        assertEquals("web", event.request().source());
        
        // The key assertion: user information should be present
        assertNotNull(event.attack().user(), "User should be captured in attack wave event");
        assertEquals("user123", event.attack().user().id());
        assertEquals("John Doe", event.attack().user().name());
    }

    @Test
    public void testAttackWaveDetectionWithoutUser() throws SQLException, InterruptedException {
        // Setup
        ServiceConfigStore.updateFromAPIResponse(emptyAPIResponse);
        ServiceConfigStore.updateFromAPIListsResponse(emptyAPIListsResponse);
        
        // Create attack wave context without user
        ContextObject attackWaveCtx = new EmptySampleContextObject("/wp-config.php", "BADMETHOD", Map.of());
        
        // Trigger attack waves by making multiple requests
        for (int i = 0; i < 15; i++) {
            Context.set(attackWaveCtx);
            WebRequestCollector.report(attackWaveCtx);
        }
        
        // Call WebResponseCollector which should detect the attack wave without user info
        Context.set(attackWaveCtx);
        WebResponseCollector.report(200);
        
        // Verify attack wave was detected
        assertTrue(AttackQueue.getSize() > 0, "Attack wave should be detected");
        
        // Get the attack wave event
        DetectedAttackWave.DetectedAttackWaveEvent event = 
            (DetectedAttackWave.DetectedAttackWaveEvent) AttackQueue.get();
        
        assertNotNull(event);
        assertEquals("detected_attack_wave", event.type());
        
        // User should be null when not set
        assertNull(event.attack().user(), "User should be null when not set during request");
    }
}

