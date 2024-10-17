package dev.aikido.AikidoAgent.background.ipc_commands;

import dev.aikido.AikidoAgent.background.cloud.CloudConnectionManager;

import java.util.Optional;

/**
 * Command interface for IPC Commands
 * E.g. AttackCommand : gives false in returnsData(), matches it's name for "ATTACK" and executes code to report attacks.
 */
public interface Command {
    public boolean returnsData();
    public boolean matchesName(String command);
    public Optional<String> execute(String data, CloudConnectionManager connectionManager);

}
