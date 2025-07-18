package dev.aikido.agent_api.ratelimiting;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.context.RouteMetadata;
import dev.aikido.agent_api.context.User;
import dev.aikido.agent_api.storage.RateLimiterStore;
import dev.aikido.agent_api.storage.ServiceConfigStore;

import java.util.List;

import static dev.aikido.agent_api.helpers.patterns.MatchEndpoints.matchEndpoints;
import static dev.aikido.agent_api.ratelimiting.RateLimitedEndpointFinder.getRateLimitedEndpoint;

public final class ShouldRateLimit {
    private ShouldRateLimit() {}

    public record RateLimitDecision(
        boolean block,
        String trigger,
        Endpoint rateLimitedEndpoint
    ) {
    }

    public static RateLimitDecision shouldRateLimit(RouteMetadata routeMetadata, User user, String remoteAddress) {
        List<Endpoint> endpoints = ServiceConfigStore.getConfig().getEndpoints();
        List<Endpoint> matches = matchEndpoints(routeMetadata, endpoints);
        Endpoint rateLimitedEndpoint = getRateLimitedEndpoint(matches, routeMetadata.route());
        if (rateLimitedEndpoint == null) {
            return new RateLimitDecision(/*block*/false, null, null);
        }

        long windowSizeInMS = rateLimitedEndpoint.getRateLimiting().windowSizeInMS();
        long maxRequests = rateLimitedEndpoint.getRateLimiting().maxRequests();
        if (user != null) {
            String key = rateLimitedEndpoint.getMethod() + ":" + rateLimitedEndpoint.getRoute() + ":user:" + user.id();
            boolean allowed = RateLimiterStore.isAllowed(key, windowSizeInMS, maxRequests);
            if (allowed) {
                // Do not continue to check based on IP if user is present:
                return new RateLimitDecision(/*block*/false, null, null);
            }
            return new RateLimitDecision(/*block*/ true, /*trigger*/ "user", rateLimitedEndpoint);
        }
        if (remoteAddress != null && !remoteAddress.isEmpty()) {
            String key = rateLimitedEndpoint.getMethod() + ":" + rateLimitedEndpoint.getRoute() + ":ip:" + remoteAddress;
            boolean allowed = RateLimiterStore.isAllowed(key, windowSizeInMS, maxRequests);
            if (!allowed) {
                return new RateLimitDecision(/*block*/ true, /*trigger*/ "ip", rateLimitedEndpoint);
            }
        }
        return new RateLimitDecision(/*block*/false, null, null);
    }
}
