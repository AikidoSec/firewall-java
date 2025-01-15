package dev.aikido.agent_api.background.ipc_commands;

import com.google.gson.Gson;
import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.helpers.extraction.ByteArrayHelper;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

import static dev.aikido.agent_api.helpers.extraction.ByteArrayHelper.splitByteArray;

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
        commands.add(new ApiDiscoveryCommand());
    }

    /**
     * Parses input string as an IPC command.
     * @param input raw IPC command.
     * -> E.g. input = "ATTACK${'this': 'that'}"
     */
    public Optional<byte[]> parseIPCInput(byte[] input) {
        ByteArrayHelper.CommandData commandData = splitByteArray(input, (byte) '$');
        if (commandData == null) {
            logger.debug("Separator not found for malformed IPC command");
            return Optional.empty();
        }
        return switchCommands(commandData.command(), commandData.data());
    }

    public Optional<byte[]> switchCommands(String commandName, byte[] data) {
        for (Command command: commands) {
            if (command.matchesName(commandName)) {
                try {
                    // Parse input for the background process into the input object of the command :
                    Gson gson = new Gson();
                    Object deserializedInput = gson.fromJson(new String(data, StandardCharsets.UTF_8), command.getInputClass());

                    Optional<?> commandResult = command.execute(deserializedInput, this.connectionManager);
                    if (command.returnsData()) {
                        // Serialize returned data into a byte[] JSON :
                        return Optional.of(gson.toJson(commandResult.get()).getBytes(StandardCharsets.UTF_8));
                    }
                } catch (Throwable e) {
                    logger.trace(e);
                }
                return Optional.empty();
            }
        }
        logger.debug("Command not found: %s", new Gson().toJson(commandName));
        return Optional.empty();
    }
}
