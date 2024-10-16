package dev.aikido.AikidoAgent.background.ipc_commands;

import dev.aikido.AikidoAgent.background.cloud.CloudConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Routes the string command input to the correct class
 */
public class CommandRouter {
    private static final Logger logger = LogManager.getLogger(CommandRouter.class);
    private static final Command[] commands = {new AttackCommand()};
    private final CloudConnectionManager connectionManager;
    public CommandRouter(CloudConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * Parses input string as an IPC command.
     * @param input raw IPC command.
     * -> E.g. input = "ATTACK${'this': 'that'}"
     */
    public void parseIPCInput(String input) {
        // P
        int indexOfCommandSeparator = input.indexOf('$');
        if (indexOfCommandSeparator == -1) {
            logger.debug("Separator not found for malformed IPC command: {}", input);
            return;
        }
        String command = input.substring(0, indexOfCommandSeparator);
        String data = input.substring(indexOfCommandSeparator + 1);
        switchCommands(command, data);
    }

    public void switchCommands(String commandName, String data) {
        for (Command command: commands) {
            if (command.matchesName(commandName)) {
                command.execute(data, this.connectionManager);
                break;
            }
        }
    }
}
