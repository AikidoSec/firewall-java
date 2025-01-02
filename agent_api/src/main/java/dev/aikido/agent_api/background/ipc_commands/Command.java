package dev.aikido.agent_api.background.ipc_commands;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.utilities.ThreadIPCClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.text.html.Option;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static dev.aikido.agent_api.helpers.Serializer.deserializeData;
import static dev.aikido.agent_api.helpers.Serializer.serializeData;
import static dev.aikido.agent_api.helpers.extraction.ByteArrayHelper.joinByteArrays;

/**
 * Command interface for IPC Commands
 * E.g. AttackCommand : gives false in returnsData(), matches it's name for "ATTACK" and executes code to report attacks.
 */
public abstract class Command<I, O> {
    private static final Logger logger = LogManager.getLogger(Command.class);

    public record EmptyResult() implements Serializable {}
    public abstract boolean returnsData();
    public abstract String getName();

    public boolean matchesName(String command) {
        return this.getName().equalsIgnoreCase(command);
    };

    public abstract Optional<O> execute(I data, CloudConnectionManager connectionManager);

    public Optional<O> send(ThreadIPCClient threadClient, I input) {
        try {
            byte[] inputAsBytes = serializeData(input);
            byte[] identifier = (getName() + "$").getBytes(StandardCharsets.UTF_8);
            Optional<byte[]> response = threadClient.send(joinByteArrays(identifier, inputAsBytes), returnsData());
            if(!response.isEmpty()) {
                O data = deserializeData(response.get());
                return Optional.of(data);
            }
        } catch (Exception e) {
            logger.trace(e);
        }
        return Optional.empty();
    };

    public I deserializeInput(byte[] input) {
        try {
            return deserializeData(input);
        } catch (Exception e) {
            logger.trace(e);
            return null;
        }
    };
}
