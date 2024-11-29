package dev.aikido.agent_api.ratelimiting;

import dev.aikido.agent_api.background.Endpoint;

import java.util.ArrayList;
import java.util.List;

public final class RateLimitedEndpointFinder {
    private RateLimitedEndpointFinder() {}
    public static Endpoint getRateLimitedEndpoint(List<Endpoint> endpoints, String route) {
        if (endpoints == null || endpoints.isEmpty()) {
            return null;
        }

        List<Endpoint> matches = new ArrayList<>();
        for (Endpoint endpoint : endpoints) {
            Endpoint.RateLimitingConfig rateLimiting = endpoint.getRateLimiting();
            if (rateLimiting != null && rateLimiting.enabled()) {
                if (endpoint.getRoute().equals(route)) {
                    // Exact match, return the exact match
                    return endpoint;
                }
                matches.add(endpoint);
            }
        }
        if (matches.isEmpty()) {
            return null;
        }
        matches.sort((a, b) -> {
            double rateA = getEndpointRateLimitingRate(a);
            double rateB = getEndpointRateLimitingRate(b);
            return Double.compare(rateA, rateB);
        });

        return matches.get(0);
    }

    public static double getEndpointRateLimitingRate(Endpoint endpoint) {
        Endpoint.RateLimitingConfig rateLimiting = endpoint.getRateLimiting();
        long maxRequests = rateLimiting.maxRequests();
        long windowMs = rateLimiting.windowSizeInMS();
        return (double) maxRequests / windowMs;
    }
}
