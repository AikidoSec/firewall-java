package dev.aikido.agent_api.background.ipc_commands;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.context.RouteMetadata;
import dev.aikido.agent_api.context.User;
import dev.aikido.agent_api.ratelimiting.ShouldRateLimit;

import java.util.Optional;

import static dev.aikido.agent_api.ratelimiting.ShouldRateLimit.shouldRateLimit;

public class ShouldRateLimitCommand extends Command<ShouldRateLimitCommand.Req, ShouldRateLimit.RateLimitDecision> {
    public record Req(RouteMetadata routeMetadata, User user, String remoteAddress) {}
    @Override
    public boolean returnsData() {
        return true; // Returns a record with data on whether to rate-limit.
    }

    @Override
    public String getName() { return "SHOULD_RATE_LIMIT"; }

    /**
     * @return an {@code RateLimitDecision} record.
     */
    @Override
    public Optional<ShouldRateLimit.RateLimitDecision> execute(Req request, CloudConnectionManager connectionManager) {
        ShouldRateLimit.RateLimitDecision response = shouldRateLimit(
                request.routeMetadata(), request.user(), request.remoteAddress(), connectionManager
        );
        return Optional.of(response);
    }
}
