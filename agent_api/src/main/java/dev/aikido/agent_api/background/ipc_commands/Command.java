package dev.aikido.agent_api.background.ipc_commands;

import com.google.gson.Gson;
import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.utilities.ThreadIPCClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static dev.aikido.agent_api.helpers.extraction.ByteArrayHelper.joinByteArrays;

/**
 * Command interface for IPC Commands
 * E.g. AttackCommand : gives false in returnsData(), matches it's name for "ATTACK" and executes code to report attacks.
 */
public abstract class Command<I, O> {
    private static final Logger logger = LogManager.getLogger(Command.class);

    public record EmptyResult() {}
    public abstract boolean returnsData();
    public abstract String getName();
    public abstract Class<I> getInputClass();
    public abstract Class<O> getOutputClass();

    public boolean matchesName(String command) {
        return this.getName().equalsIgnoreCase(command);
    };

    public abstract Optional<O> execute(I data, CloudConnectionManager connectionManager);

    public Optional<O> send(ThreadIPCClient threadClient, I input) {
        try {
            // Convert input data from thread to a byte[] JSON :
            Gson gson = new Gson();
            byte[] inputAsBytes = gson.toJson(input).getBytes(StandardCharsets.UTF_8);

            byte[] identifier = (getName() + "$").getBytes(StandardCharsets.UTF_8);
            Optional<byte[]> response = threadClient.send(joinByteArrays(identifier, inputAsBytes), returnsData());

            if(!response.isEmpty()) {
                // Convert background process' response from byte[] JSON to the output object :
                O data = gson.fromJson(new String(response.get(), StandardCharsets.UTF_8), getOutputClass());
                return Optional.of(data);
            }
        } catch (Exception e) {
            logger.trace(e);
        }
        return Optional.empty();
    };
}
