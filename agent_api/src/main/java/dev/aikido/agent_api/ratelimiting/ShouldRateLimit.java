package dev.aikido.agent_api.ratelimiting;

import static dev.aikido.agent_api.helpers.patterns.MatchEndpoints.matchEndpoints;
import static dev.aikido.agent_api.ratelimiting.RateLimitedEndpointFinder.getRateLimitedEndpoint;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.context.RouteMetadata;
import dev.aikido.agent_api.context.User;
import java.util.List;

public final class ShouldRateLimit {
    private ShouldRateLimit() {}

    public record RateLimitDecision(boolean block, String trigger) {}

    public static RateLimitDecision shouldRateLimit(
            RouteMetadata routeMetadata, User user, String remoteAddress, CloudConnectionManager connectionManager) {
        List<Endpoint> endpoints = connectionManager.getConfig().getEndpoints();
        List<Endpoint> matches = matchEndpoints(routeMetadata, endpoints);
        Endpoint rateLimitedEndpoint = getRateLimitedEndpoint(matches, routeMetadata.route());
        if (rateLimitedEndpoint == null) {
            return new RateLimitDecision(/*block*/ false, null);
        }

        boolean bypassedIP = connectionManager.getConfig().getBypassedIPs().contains(remoteAddress);
        if (bypassedIP) {
            return new RateLimitDecision(/*block*/ false, null); // No rate-limiting for bypassed IPs
        }
        long windowSizeInMS = rateLimitedEndpoint.getRateLimiting().windowSizeInMS();
        long maxRequests = rateLimitedEndpoint.getRateLimiting().maxRequests();
        if (user != null) {
            String key = rateLimitedEndpoint.getMethod() + ":" + rateLimitedEndpoint.getRoute() + ":user:" + user.id();
            boolean allowed = connectionManager.getRateLimiter().isAllowed(key, windowSizeInMS, maxRequests);
            if (allowed) {
                // Do not continue to check based on IP if user is present:
                return new RateLimitDecision(/*block*/ false, null);
            }
            return new RateLimitDecision(/*block*/ true, /*trigger*/ "user");
        }
        if (remoteAddress != null && !remoteAddress.isEmpty()) {
            String key =
                    rateLimitedEndpoint.getMethod() + ":" + rateLimitedEndpoint.getRoute() + ":ip:" + remoteAddress;
            boolean allowed = connectionManager.getRateLimiter().isAllowed(key, windowSizeInMS, maxRequests);
            if (!allowed) {
                return new RateLimitDecision(/*block*/ true, /*trigger*/ "ip");
            }
        }
        return new RateLimitDecision(/*block*/ false, null);
    }
}
