package dev.aikido.agent_api.background.ipc_commands;

import com.google.gson.Gson;
import dev.aikido.agent_api.background.cloud.CloudConnectionManager;

import java.util.Optional;

public class BlockingEnabledCommand implements  Command {
    public record BlockingEnabledResult(boolean isBlockingEnabled) {}

    @Override
    public boolean returnsData() {
        // This command returns a boolean value (Whether blocking is enabled or not)
        return true;
    }

    @Override
    public boolean matchesName(String command) {
        return command.equalsIgnoreCase("BLOCKING_ENABLED");
    }

    @Override
    public Optional<String> execute(String data, CloudConnectionManager connectionManager) {
        // "data" can be safely ignored, we just need to return if blocking is enabled
        boolean isBlockingEnabled = connectionManager.shouldBlock();

        Gson gson = new Gson();
        String result = gson.toJson(new BlockingEnabledResult(isBlockingEnabled));
        return Optional.of(result);
    }
}
