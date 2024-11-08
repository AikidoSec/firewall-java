package dev.aikido.agent_api.background.ipc_commands;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

/**
 * Routes the string command input to the correct class
 */
public class CommandRouter {
    private static final Logger logger = LogManager.getLogger(CommandRouter.class);
    private final List<Command> commands = new ArrayList<>();

    private final CloudConnectionManager connectionManager;
    public CommandRouter(CloudConnectionManager connectionManager, BlockingQueue<APIEvent> queue) {
        this.connectionManager = connectionManager;
        commands.add(new BlockingEnabledCommand());
        commands.add(new InitRouteCommand());
        commands.add(new SyncDataCommand());
        commands.add(new ShouldRateLimitCommand());
        commands.add(new RegisterUserCommand());
        commands.add(new AttackCommand(queue));
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
