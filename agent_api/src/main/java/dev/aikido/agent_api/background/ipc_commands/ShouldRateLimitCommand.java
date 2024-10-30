package dev.aikido.agent_api.background.ipc_commands;

import com.google.gson.Gson;
import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.context.RouteMetadata;
import dev.aikido.agent_api.context.User;
import dev.aikido.agent_api.ratelimiting.ShouldRateLimit;

import java.util.Optional;

import static dev.aikido.agent_api.ratelimiting.ShouldRateLimit.shouldRateLimit;

public class ShouldRateLimitCommand implements Command {
    public record Req(RouteMetadata routeMetadata, User user, String remoteAddress) {}
    @Override
    public boolean returnsData() {
        return true; // Returns a record with data on whether to rate-limit.
    }

    @Override
    public boolean matchesName(String command) {
        return command.equalsIgnoreCase("SHOULD_RATE_LIMIT");
    }

    /**
     * @param data is a JSON string that represents a {@code Req} record.
     * @return a JSON string that represents a {@code RateLimitDecision} record.
     */
    @Override
    public Optional<String> execute(String data, CloudConnectionManager connectionManager) {
        Gson gson = new Gson();
        Req request = gson.fromJson(data, Req.class);
        ShouldRateLimit.RateLimitDecision response = shouldRateLimit(request.routeMetadata(), request.user(), request.remoteAddress(), connectionManager);
        if (response == null) {
            return Optional.empty();
        }
        String jsonResponse = gson.toJson(response);
        return Optional.of(jsonResponse);
    }
}
