package dev.aikido.agent_api.background;

import dev.aikido.agent_api.background.ipc_commands.CommandRouter;
import org.newsclub.net.unix.AFUNIXServerSocket;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static dev.aikido.agent_api.background.utilities.IPCFacilitator.*;

public class IPCServer {
    private final AFUNIXServerSocket serverSocket;
    private final CommandRouter commandRouter;

    public IPCServer(Path socketPath, BackgroundProcess process) throws IOException, InterruptedException {
        // Delete previous socket file :
        Files.deleteIfExists(socketPath); // Make sure this is alright with multiple agents

        commandRouter = new CommandRouter(process.getCloudConnectionManager());

        // Create a new server socket :
        serverSocket = AFUNIXServerSocket.newInstance();
        serverSocket.bind(AFUNIXSocketAddress.of(socketPath.toFile()));
        this.listen();
    }
    private void listen() throws IOException, InterruptedException {
        while (!serverSocket.isClosed()) {
            AFUNIXSocket channel = serverSocket.accept();
            Optional<String> message = readFromSocket(channel);
            if (message.isEmpty()) {
                continue;
            }
            Optional<String> response = commandRouter.parseIPCInput(message.get());
            if (response.isPresent() && channel.isConnected()) {
                writeToSocket(channel, response.get()); // Write response
            }
            channel.close();
            Thread.sleep(10);
        }
    }
}
