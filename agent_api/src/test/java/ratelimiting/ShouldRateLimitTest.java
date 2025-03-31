package ratelimiting;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.background.ServiceConfiguration;
import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.context.RouteMetadata;
import dev.aikido.agent_api.context.User;
import dev.aikido.agent_api.ratelimiting.ShouldRateLimit;
import dev.aikido.agent_api.ratelimiting.SlidingWindowRateLimiter;
import dev.aikido.agent_api.storage.RateLimiterStore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShouldRateLimitTest {
    private CloudConnectionManager connectionManager;

    private RouteMetadata createRouteMetadata(String method, String route) {
        return new RouteMetadata(route, route, method);
    }

    @AfterAll
    public static void cleanup() {
        RateLimiterStore.clear();
    }
    @BeforeEach
    public void clearRateLimiter() {
        RateLimiterStore.clear();
    }

    private CloudConnectionManager createConnectionManager(List<Endpoint> endpoints, HashSet<String> bypassedIPs) {
        // Mock the configuration object
        ServiceConfiguration configMock = Mockito.mock(ServiceConfiguration.class);
        Mockito.when(configMock.getEndpoints()).thenReturn(endpoints);
        Mockito.when(configMock.getBypassedIPs()).thenReturn(bypassedIPs);

        // Mock the CloudConnectionManager
        CloudConnectionManager cm = Mockito.mock(CloudConnectionManager.class);
        Mockito.when(cm.getConfig()).thenReturn(configMock);
        Mockito.when(cm.getRateLimiter()).thenReturn(new SlidingWindowRateLimiter(5000, 120*60*1000));

        return cm;
    }


    @Test
    public void testRateLimitsByIp() {
        List<Endpoint> endpoints = new ArrayList<>();
        endpoints.add(new Endpoint("POST", "/login",
                /*maxRequests*/ 3, /*windowSizeMS*/ 1000, List.of(),
                false, false, true));

        connectionManager = createConnectionManager(endpoints, new HashSet<>());

        RouteMetadata routeMetadata = createRouteMetadata("POST", "/login");
        String remoteAddress = "1.2.3.4";

        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(routeMetadata, null, remoteAddress, connectionManager));
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(routeMetadata, null, remoteAddress, connectionManager));
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(routeMetadata, null, remoteAddress, connectionManager));
        assertEquals(new ShouldRateLimit.RateLimitDecision(true, "ip"),
                ShouldRateLimit.shouldRateLimit(routeMetadata, null, remoteAddress, connectionManager));
    }

    @Test
    public void testRateLimitingByUser() {
        List<Endpoint> endpoints = new ArrayList<>();
        endpoints.add(new Endpoint("POST", "/login",
                /*maxRequests*/ 3, /*windowSizeMS*/ 1000, List.of(),
                false, false, true));

        connectionManager = createConnectionManager(endpoints, new HashSet<>());

        RouteMetadata routeMetadata = createRouteMetadata("POST", "/login");
        User user = new User("user123", "John Doe", "1.1.1.1", 0);

        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(routeMetadata, user, "1.2.3.4", connectionManager));
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(routeMetadata, user, "1.2.3.5", connectionManager));
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(routeMetadata, user, "1.2.3.6", connectionManager));
        assertEquals(new ShouldRateLimit.RateLimitDecision(true, "user"),
                ShouldRateLimit.shouldRateLimit(routeMetadata, user, "1.2.3.7", connectionManager));
    }

    @Test
    public void testRateLimitingWithWildcard() {
        List<Endpoint> endpoints = new ArrayList<>();
        endpoints.add(new Endpoint("POST", "/api/*",
                /*maxRequests*/ 3, /*windowSizeMS*/ 1000, List.of(),
                false, false, true));

        connectionManager = createConnectionManager(endpoints, new HashSet<>());

        // Test requests to different API endpoints
        RouteMetadata routeMetadataLogin = createRouteMetadata("POST", "/api/login");
        RouteMetadata routeMetadataLogout = createRouteMetadata("POST", "/api/logout");
        RouteMetadata routeMetadataResetPassword = createRouteMetadata("POST", "/api/reset-password");

        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(routeMetadataLogin, null, "1.2.3.4", connectionManager));
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(routeMetadataLogout, null, "1.2.3.4", connectionManager));
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(routeMetadataResetPassword, null, "1.2.3.4", connectionManager));

        // This request should trigger the rate limit
        assertEquals(new ShouldRateLimit.RateLimitDecision(true, "ip"),
                ShouldRateLimit.shouldRateLimit(routeMetadataLogin, null, "1.2.3.4", connectionManager));
    }

    @Test
    public void testRateLimitingByUserWithSameIp() {
        List<Endpoint> endpoints = new ArrayList<>();
        endpoints.add(new Endpoint("POST", "/login",
                /*maxRequests*/ 3, /*windowSizeMS*/ 1000, List.of(),
                false, false, true));

        connectionManager = createConnectionManager(endpoints, new HashSet<>());

        RouteMetadata routeMetadata = createRouteMetadata("POST", "/login");
        User user = new User("user123", "John Doe", "1.2.3.4", 0);

        // First three requests should not be blocked
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(routeMetadata, user, "1.2.3.4", connectionManager));
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(routeMetadata, user, "1.2.3.4", connectionManager));
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(routeMetadata, user, "1.2.3.4", connectionManager));

        // This request should trigger the rate limit
        assertEquals(new ShouldRateLimit.RateLimitDecision(true, "user"),
                ShouldRateLimit.shouldRateLimit(routeMetadata, user, "1.2.3.4", connectionManager));
    }

    @Test
    public void testRateLimitingWithWildcard2() {
        List<Endpoint> endpoints = new ArrayList<>();
        endpoints.add(new Endpoint("*", "/api/*",
                /*maxRequests*/ 3, /*windowSizeMS*/ 1000, List.of(),
                false, false, true));

        CloudConnectionManager connectionManager = createConnectionManager(endpoints, new HashSet<>());

        // Test requests to different API endpoints with various methods
        RouteMetadata metadata = createRouteMetadata("POST", "/api/login");
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(metadata, null, "1.2.3.4", connectionManager));

        metadata = createRouteMetadata("GET", "/api/logout");
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(metadata, null, "1.2.3.4", connectionManager));

        metadata = createRouteMetadata("PUT", "/api/reset-password");
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(metadata, null, "1.2.3.4", connectionManager));

        // This request should trigger the rate limit
        metadata = createRouteMetadata("GET", "/api/login");
        assertEquals(new ShouldRateLimit.RateLimitDecision(true, "ip"),
                ShouldRateLimit.shouldRateLimit(metadata, null, "1.2.3.4", connectionManager));
    }

    @Test
    public void testRateLimitingByUserWithDifferentIps() {
        List<Endpoint> endpoints = new ArrayList<>();
        endpoints.add(new Endpoint("POST", "/login",
                /*maxRequests*/ 3, /*windowSizeMS*/ 1000, List.of(),
                false, false, true));

        CloudConnectionManager connectionManager = createConnectionManager(endpoints, new HashSet<>());

        // First request from first IP
        RouteMetadata metadata = createRouteMetadata("POST", "/login");
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(metadata, new User("123", "User 123", "1.2.3.4", 0), "1.2.3.4", connectionManager));

        // First request from second IP
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(metadata, new User("123", "User 123", "4.3.2.1", 0), "4.3.2.1", connectionManager));

        // Second request from first IP
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(metadata, new User("123", "User 123", "1.2.3.4", 0), "1.2.3.4", connectionManager));

        // This request from second IP should trigger the rate limit
        assertEquals(new ShouldRateLimit.RateLimitDecision(true, "user"),
                ShouldRateLimit.shouldRateLimit(metadata, new User("123", "User 123", "4.3.2.1", 0), "4.3.2.1", connectionManager));
    }

    @Test
    public void testRateLimitingSameIpDifferentUsers() {
        List<Endpoint> endpoints = new ArrayList<>();
        endpoints.add(new Endpoint("POST", "/login",
                /*maxRequests*/ 3, /*windowSizeMS*/ 1000, List.of(),
                false, false, true));

        CloudConnectionManager connectionManager = createConnectionManager(endpoints, new HashSet<>());

        // First request from user 1
        RouteMetadata metadata = createRouteMetadata("POST", "/login");
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(metadata, new User("123", "User 123", "1.2.3.4", 0), "1.2.3.4", connectionManager));

        // First request from user 2
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(metadata, new User("123456", "User 456", "1.2.3.4", 0), "1.2.3.4", connectionManager));

        // Second request from user 1
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(metadata, new User("123", "User 123", "1.2.3.4", 0), "1.2.3.4", connectionManager));

        // Second request from user 2
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(metadata, new User("123456", "User 456", "1.2.3.4", 0), "1.2.3.4", connectionManager));
    }
}