package dev.aikido.agent_api.background.ipc_commands;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.storage.Hostnames;

import java.util.Map;
import java.util.Optional;

/** UpdateAgentDataCommand
 * This command UPDATE_AGENT_DATA is responsible for updating route hits, total hits and executed middleware status.
 */
public class UpdateAgentDataCommand extends Command<UpdateAgentDataCommand.Res, Command.EmptyResult> {
    public record Res(Hostnames.HostnameEntry[] hostnames) {};

    @Override
    public boolean returnsData() {
        return false;
    }

    @Override
    public String getName() {
        return "UPDATE_AGENT_DATA";
    }

    @Override
    public Class<Res> getInputClass() {
        return Res.class;
    }

    @Override
    public Class<EmptyResult> getOutputClass() {
        return EmptyResult.class;
    }

    @Override
    public Optional<EmptyResult> execute(Res data, CloudConnectionManager connectionManager) {
        // Update middleware installed,
        if (data.hostnames != null) {
            connectionManager.getHostnames().addArray(data.hostnames);
        }

        return Optional.empty();
    }
}
