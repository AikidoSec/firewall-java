package dev.aikido.AikidoAgent.background.ipc_commands;

import dev.aikido.AikidoAgent.background.cloud.CloudConnectionManager;

public interface Command {
    public boolean returnsData();
    public boolean matchesName(String command);
    public void execute(String data, CloudConnectionManager connectionManager);

}
