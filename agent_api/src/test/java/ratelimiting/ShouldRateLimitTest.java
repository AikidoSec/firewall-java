package ratelimiting;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.context.RouteMetadata;
import dev.aikido.agent_api.context.User;
import dev.aikido.agent_api.ratelimiting.ShouldRateLimit;
import dev.aikido.agent_api.storage.RateLimiterStore;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static utils.EmptyAPIResponses.emptyAPIResponse;
import static utils.EmptyAPIResponses.setEmptyConfigWithEndpointList;


public class ShouldRateLimitTest {
    private CloudConnectionManager connectionManager;

    private RouteMetadata createRouteMetadata(String method, String route) {
        return new RouteMetadata(route, route, method);
    }

    @AfterAll
    public static void cleanup() {
        ServiceConfigStore.updateFromAPIResponse(emptyAPIResponse);
        RateLimiterStore.clear();
    }
    @BeforeEach
    public void clearRateLimiter() {
        RateLimiterStore.clear();
    }


    @Test
    public void testRateLimitsByIp() {
        List<Endpoint> endpoints = new ArrayList<>();
        endpoints.add(new Endpoint("POST", "/login",
                /*maxRequests*/ 3, /*windowSizeMS*/ 1000, List.of(),
                false, false, true));
        setEmptyConfigWithEndpointList(endpoints);

        RouteMetadata routeMetadata = createRouteMetadata("POST", "/login");
        String remoteAddress = "1.2.3.4";

        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(routeMetadata, null, remoteAddress));
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(routeMetadata, null, remoteAddress));
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(routeMetadata, null, remoteAddress));
        assertEquals(new ShouldRateLimit.RateLimitDecision(true, "ip"),
                ShouldRateLimit.shouldRateLimit(routeMetadata, null, remoteAddress));
    }

    @Test
    public void testRateLimitingByUser() {
        List<Endpoint> endpoints = new ArrayList<>();
        endpoints.add(new Endpoint("POST", "/login",
                /*maxRequests*/ 3, /*windowSizeMS*/ 1000, List.of(),
                false, false, true));
        setEmptyConfigWithEndpointList(endpoints);

        RouteMetadata routeMetadata = createRouteMetadata("POST", "/login");
        User user = new User("user123", "John Doe", "1.1.1.1", 0);

        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(routeMetadata, user, "1.2.3.4"));
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(routeMetadata, user, "1.2.3.5"));
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(routeMetadata, user, "1.2.3.6"));
        assertEquals(new ShouldRateLimit.RateLimitDecision(true, "user"),
                ShouldRateLimit.shouldRateLimit(routeMetadata, user, "1.2.3.7"));
    }

    @Test
    public void testRateLimitingWithWildcard() {
        List<Endpoint> endpoints = new ArrayList<>();
        endpoints.add(new Endpoint("POST", "/api/*",
                /*maxRequests*/ 3, /*windowSizeMS*/ 1000, List.of(),
                false, false, true));
        setEmptyConfigWithEndpointList(endpoints);

        // Test requests to different API endpoints
        RouteMetadata routeMetadataLogin = createRouteMetadata("POST", "/api/login");
        RouteMetadata routeMetadataLogout = createRouteMetadata("POST", "/api/logout");
        RouteMetadata routeMetadataResetPassword = createRouteMetadata("POST", "/api/reset-password");

        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(routeMetadataLogin, null, "1.2.3.4"));
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(routeMetadataLogout, null, "1.2.3.4"));
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(routeMetadataResetPassword, null, "1.2.3.4"));

        // This request should trigger the rate limit
        assertEquals(new ShouldRateLimit.RateLimitDecision(true, "ip"),
                ShouldRateLimit.shouldRateLimit(routeMetadataLogin, null, "1.2.3.4"));
    }

    @Test
    public void testRateLimitingByUserWithSameIp() {
        List<Endpoint> endpoints = new ArrayList<>();
        endpoints.add(new Endpoint("POST", "/login",
                /*maxRequests*/ 3, /*windowSizeMS*/ 1000, List.of(),
                false, false, true));
        setEmptyConfigWithEndpointList(endpoints);

        RouteMetadata routeMetadata = createRouteMetadata("POST", "/login");
        User user = new User("user123", "John Doe", "1.2.3.4", 0);

        // First three requests should not be blocked
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(routeMetadata, user, "1.2.3.4"));
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(routeMetadata, user, "1.2.3.4"));
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(routeMetadata, user, "1.2.3.4"));

        // This request should trigger the rate limit
        assertEquals(new ShouldRateLimit.RateLimitDecision(true, "user"),
                ShouldRateLimit.shouldRateLimit(routeMetadata, user, "1.2.3.4"));
    }

    @Test
    public void testRateLimitingWithWildcard2() {
        List<Endpoint> endpoints = new ArrayList<>();
        endpoints.add(new Endpoint("*", "/api/*",
                /*maxRequests*/ 3, /*windowSizeMS*/ 1000, List.of(),
                false, false, true));
        setEmptyConfigWithEndpointList(endpoints);

        // Test requests to different API endpoints with various methods
        RouteMetadata metadata = createRouteMetadata("POST", "/api/login");
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(metadata, null, "1.2.3.4"));

        metadata = createRouteMetadata("GET", "/api/logout");
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(metadata, null, "1.2.3.4"));

        metadata = createRouteMetadata("PUT", "/api/reset-password");
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(metadata, null, "1.2.3.4"));

        // This request should trigger the rate limit
        metadata = createRouteMetadata("GET", "/api/login");
        assertEquals(new ShouldRateLimit.RateLimitDecision(true, "ip"),
                ShouldRateLimit.shouldRateLimit(metadata, null, "1.2.3.4"));
    }

    @Test
    public void testRateLimitingByUserWithDifferentIps() {
        List<Endpoint> endpoints = new ArrayList<>();
        endpoints.add(new Endpoint("POST", "/login",
                /*maxRequests*/ 3, /*windowSizeMS*/ 1000, List.of(),
                false, false, true));
        setEmptyConfigWithEndpointList(endpoints);

        // First request from first IP
        RouteMetadata metadata = createRouteMetadata("POST", "/login");
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(metadata, new User("123", "User 123", "1.2.3.4", 0), "1.2.3.4"));

        // First request from second IP
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(metadata, new User("123", "User 123", "4.3.2.1", 0), "4.3.2.1"));

        // Second request from first IP
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(metadata, new User("123", "User 123", "1.2.3.4", 0), "1.2.3.4"));

        // This request from second IP should trigger the rate limit
        assertEquals(new ShouldRateLimit.RateLimitDecision(true, "user"),
                ShouldRateLimit.shouldRateLimit(metadata, new User("123", "User 123", "4.3.2.1", 0), "4.3.2.1"));
    }

    @Test
    public void testRateLimitingSameIpDifferentUsers() {
        List<Endpoint> endpoints = new ArrayList<>();
        endpoints.add(new Endpoint("POST", "/login",
                /*maxRequests*/ 3, /*windowSizeMS*/ 1000, List.of(),
                false, false, true));
        setEmptyConfigWithEndpointList(endpoints);

        // First request from user 1
        RouteMetadata metadata = createRouteMetadata("POST", "/login");
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(metadata, new User("123", "User 123", "1.2.3.4", 0), "1.2.3.4"));

        // First request from user 2
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(metadata, new User("123456", "User 456", "1.2.3.4", 0), "1.2.3.4"));

        // Second request from user 1
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(metadata, new User("123", "User 123", "1.2.3.4", 0), "1.2.3.4"));

        // Second request from user 2
        assertEquals(new ShouldRateLimit.RateLimitDecision(false, null),
                ShouldRateLimit.shouldRateLimit(metadata, new User("123456", "User 456", "1.2.3.4", 0), "1.2.3.4"));
    }
}