package dev.aikido.agent_api.background.ipc_commands;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 * Routes the string command input to the correct class
 */
public class CommandRouter {
    private static final Logger logger = LogManager.getLogger(CommandRouter.class);
    private static final Command[] commands = {
            new AttackCommand(),
            new BlockingEnabledCommand(),
            new InitRouteCommand(),
            new SyncDataCommand(),
            new ShouldRateLimitCommand(),
            new RegisterUserCommand()
    };
    private final CloudConnectionManager connectionManager;
    public CommandRouter(CloudConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * Parses input string as an IPC command.
     * @param input raw IPC command.
     * -> E.g. input = "ATTACK${'this': 'that'}"
     */
    public Optional<String> parseIPCInput(String input) {
        int indexOfCommandSeparator = input.indexOf('$');
        if (indexOfCommandSeparator == -1) {
            logger.debug("Separator not found for malformed IPC command: {}", input);
            return Optional.empty();
        }
        String command = input.substring(0, indexOfCommandSeparator);
        String data = input.substring(indexOfCommandSeparator + 1);
        return switchCommands(command, data);
    }

    public Optional<String> switchCommands(String commandName, String data) {
        for (Command command: commands) {
            if (command.matchesName(commandName)) {
                try {
                    Optional<String> commandResult = command.execute(data, this.connectionManager);
                    if (command.returnsData()) {
                        return commandResult;
                    }
                } catch (Throwable e) {
                    logger.trace(e);
                }
                return Optional.empty();
            }
        }
        logger.debug("Command not found: {} (With data: {})", commandName, data);
        return Optional.empty();
    }
}
