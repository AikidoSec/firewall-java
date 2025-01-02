package dev.aikido.agent_api.background.ipc_commands;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;

import java.util.Optional;

public class BlockingEnabledCommand extends Command<Command.EmptyResult, BlockingEnabledCommand.Res> {
    public record Res(boolean isBlockingEnabled) {}

    @Override
    public boolean returnsData() {
        // This command returns a boolean value (Whether blocking is enabled or not)
        return true;
    }

    @Override
    public String getName() { return "BLOCKING_ENABLED"; }

    @Override
    public Optional<Res> execute(EmptyResult data, CloudConnectionManager connectionManager) {
        // "data" can be safely ignored, we just need to return if blocking is enabled
        boolean isBlockingEnabled = connectionManager.shouldBlock();
        return Optional.of( new Res(isBlockingEnabled));
    }
}
