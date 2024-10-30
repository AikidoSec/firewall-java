package dev.aikido.agent_api.ratelimiting;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.context.RouteMetadata;
import dev.aikido.agent_api.context.User;

public class ShouldRateLimit {
    public record RateLimitDecision(boolean block, String trigger) {}
    public static RateLimitDecision shouldRateLimit(
            RouteMetadata routeMetadata, User user, String remoteAddress, CloudConnectionManager connectionManager
    ) {
        return null;
    }
}
