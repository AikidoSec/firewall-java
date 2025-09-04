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
    private static final RateLimitDecision NO_RATE_LIMIT = new RateLimitDecision(/*block*/false, null);
    public record RateLimitDecision(boolean block, String trigger) {}
    public static RateLimitDecision shouldRateLimit(RouteMetadata routeMetadata, User user, String rateLimitGroup, String remoteAddress) {
        List<Endpoint> endpoints = ServiceConfigStore.getConfig().getEndpoints();
        List<Endpoint> matches = matchEndpoints(routeMetadata, endpoints);
        Endpoint rateLimitedEndpoint = getRateLimitedEndpoint(matches, routeMetadata.route());
        if (rateLimitedEndpoint == null) {
            return NO_RATE_LIMIT;
        }

        long windowSizeInMS = rateLimitedEndpoint.getRateLimiting().windowSizeInMS();
        long maxRequests = rateLimitedEndpoint.getRateLimiting().maxRequests();

        // First check the group, then the user and finally the IP in that order.
        if (rateLimitGroup != null) {
            String key = getKeyForRateLimitGroup(rateLimitedEndpoint, rateLimitGroup);
            boolean allowed = RateLimiterStore.isAllowed(key, windowSizeInMS, maxRequests);
            if (allowed) {
                // Do not continue to check based on User ID or IP if group is set
                return NO_RATE_LIMIT;
            }
            return new RateLimitDecision(true, "group");
        }
        if (user != null) {
            String key = getKeyForUser(rateLimitedEndpoint, user);
            boolean allowed = RateLimiterStore.isAllowed(key, windowSizeInMS, maxRequests);
            if (allowed) {
                // Do not continue to check based on IP if user is present:
                return NO_RATE_LIMIT;
            }
            return new RateLimitDecision(true, "user");
        }
        if (remoteAddress != null && !remoteAddress.isEmpty()) {
            String key = getKeyForIp(rateLimitedEndpoint, remoteAddress);
            boolean allowed = RateLimiterStore.isAllowed(key, windowSizeInMS, maxRequests);
            if (!allowed) {
                return new RateLimitDecision(true, "ip");
            }
        }
        return NO_RATE_LIMIT;
    }

    // Helpers to abstract the key creation logic
    private static String getKeyForRateLimitGroup(Endpoint endpoint, String groupId) {
        // `{method}:{route}:group:{groupId}`
        return String.format("%s:%s:group:%s", endpoint.getMethod(), endpoint.getRoute(), groupId);
    }
    private static String getKeyForUser(Endpoint endpoint, User user) {
        // `{method}:{route}:user:{userId}`
        return String.format("%s:%s:user:%s", endpoint.getMethod(), endpoint.getRoute(), user.id());
    }
    private static String getKeyForIp(Endpoint endpoint, String ip) {
        // `{method}:{route}:ip:{ip}`
        return String.format("%s:%s:ip:%s", endpoint.getMethod(), endpoint.getRoute(), ip);
    }
}
