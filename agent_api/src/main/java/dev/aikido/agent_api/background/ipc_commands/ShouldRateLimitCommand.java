package dev.aikido.agent_api.background.ipc_commands;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;

import java.util.Optional;

public class ShouldRateLimitCommand implements Command {
    @Override
    public boolean returnsData() {
        return true; // Returns a record with data on whether to rate-limit.
    }

    @Override
    public boolean matchesName(String command) {
        return command.equalsIgnoreCase("SHOULD_RATE_LIMIT");
    }

    @Override
    public Optional<String> execute(String data, CloudConnectionManager connectionManager) {
        return Optional.empty();
    }
}
