package dev.aikido.agent_api.background;

import dev.aikido.agent_api.background.ipc_commands.CommandRouter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.newsclub.net.unix.AFUNIXServerSocket;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static dev.aikido.agent_api.background.utilities.IPCFacilitator.readFromSocket;
import static dev.aikido.agent_api.background.utilities.IPCFacilitator.writeToSocket;

public class BackgroundReceiver {
    public static final Logger logger = LogManager.getLogger(BackgroundReceiver.class);
    private final AFUNIXServerSocket socket;
    private final CommandRouter commandRouter;

    public BackgroundReceiver(File socketFile, BackgroundProcess process) throws IOException, InterruptedException {
        // Delete previous socket file :
        commandRouter = new CommandRouter(
            /* connection manager: */ process.getCloudConnectionManager(),
            /* attack queue: */ process.getAttackQueue()
        );

        // Create a new receiver:
        // Create a new server socket :
        socket = AFUNIXServerSocket.newInstance();
        socket.bind(AFUNIXSocketAddress.of(socketFile));
        this.listen();
    }
    private void listen() throws IOException, InterruptedException {
        while (!socket.isClosed()) {
            AFUNIXSocket channel = socket.accept();
            Optional<byte[]> message = readFromSocket(channel);
            if (message.isEmpty()) {
                continue;
            }
            Optional<byte[]> response = commandRouter.parseIPCInput(message.get());
            if (response.isPresent() && channel.isConnected()) {
                writeToSocket(channel, response.get()); // Write response
            }
            channel.close();
        }
    }
}
