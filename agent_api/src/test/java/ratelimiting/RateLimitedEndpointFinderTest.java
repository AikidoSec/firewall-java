package ratelimiting;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.ratelimiting.RateLimitedEndpointFinder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RateLimitedEndpointFinderTest {

    private List<Endpoint> createEndpoints() {
        List<Endpoint> endpoints = new ArrayList<>();
        endpoints.add(new Endpoint("POST", "/api/login", 3, 1000, Collections.emptyList(), false, false, true));
        endpoints.add(new Endpoint("POST", "/api/*", 1, 1000, Collections.emptyList(), false, false, true));
        endpoints.add(new Endpoint("GET", "/", 3, 1000, Collections.emptyList(), false, false, false));
        return endpoints;
    }

    @Test
    public void testReturnsNoneIfNoEndpoints() {
        assertNull(RateLimitedEndpointFinder.getRateLimitedEndpoint(Collections.emptyList(), "/api/login"));
    }

    @Test
    public void testReturnsNoneIfNoMatchingEndpoints() {
        assertNull(RateLimitedEndpointFinder.getRateLimitedEndpoint(Collections.emptyList(), "/nonexistent"));
    }

    @Test
    public void testReturnsNoneIfMatchingButNotEnabled() {
        List<Endpoint> endpoints = createEndpoints();
        Endpoint endpoint = new Endpoint(
                endpoints.getFirst().getMethod(),
                endpoints.getFirst().getRoute(),
                endpoints.getFirst().getRateLimiting().maxRequests(),
                endpoints.getFirst().getRateLimiting().windowSizeInMS(),
                endpoints.getFirst().getAllowedIPAddresses(),
                endpoints.getFirst().isGraphql(),
                endpoints.getFirst().protectionForcedOff(),
                false // Rate limiting disabled
                );
        assertNull(RateLimitedEndpointFinder.getRateLimitedEndpoint(List.of(endpoint), "/api/login"));
    }

    @Test
    public void testReturnsEndpointIfMatchingAndEnabled() {
        List<Endpoint> endpoints = createEndpoints();
        Endpoint result = RateLimitedEndpointFinder.getRateLimitedEndpoint(endpoints, "/api/login");
        assertEquals(endpoints.getFirst(), result);
    }

    @Test
    public void testReturnsEndpointWithLowestMaxRequests() {
        List<Endpoint> endpoints = createEndpoints();
        Endpoint result = RateLimitedEndpointFinder.getRateLimitedEndpoint(endpoints, "/api/log*");
        assertEquals(endpoints.get(1), result); // The one with maxRequests = 1
    }

    @Test
    public void testReturnsEndpointWithSmallestWindowSize() {
        List<Endpoint> endpoints = createEndpoints();
        endpoints.add(new Endpoint("POST", "/api/log*", 3, 5000, Collections.emptyList(), false, false, true));
        Endpoint result = RateLimitedEndpointFinder.getRateLimitedEndpoint(endpoints, "/api/log*");
        assertEquals(5000, result.getRateLimiting().windowSizeInMS()); // The one with the larger window size
    }

    @Test
    public void testAlwaysReturnsExactMatchesFirst() {
        List<Endpoint> endpoints = createEndpoints();
        Endpoint result = RateLimitedEndpointFinder.getRateLimitedEndpoint(endpoints, "/api/login");
        assertEquals(endpoints.getFirst(), result); // Exact match should be returned first
    }
}
