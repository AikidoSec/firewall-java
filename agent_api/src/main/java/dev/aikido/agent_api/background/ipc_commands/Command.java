package dev.aikido.agent_api.background.ipc_commands;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.utilities.ThreadClient;

import javax.swing.text.html.Option;
import java.io.*;
import java.util.Optional;

/**
 * Command interface for IPC Commands
 * E.g. AttackCommand : gives false in returnsData(), matches it's name for "ATTACK" and executes code to report attacks.
 */
public abstract class Command<I, O> {
    public record EmptyResult() {}
    public abstract boolean returnsData();
    public abstract String getName();

    public boolean matchesName(String command) {
        return this.getName().equalsIgnoreCase(command);
    };

    public abstract Optional<O> execute(I data, CloudConnectionManager connectionManager);

    public Optional<O> send(ThreadClient threadClient, I input) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(input);
            byte[] inputAsBytes = bos.toByteArray();
            Optional<byte[]> response = threadClient.send(inputAsBytes, returnsData());
            if(!response.isEmpty()) {
                return Optional.of(deserializeOutput(response.get()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    };

    public I deserializeInput(byte[] input) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(input);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (I) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    };
    private O deserializeOutput(byte[] input) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(input);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (O) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    };
}
