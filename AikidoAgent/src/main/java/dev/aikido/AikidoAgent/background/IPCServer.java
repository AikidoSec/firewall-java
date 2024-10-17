package dev.aikido.AikidoAgent.background;

import dev.aikido.AikidoAgent.background.ipc_commands.CommandRouter;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static dev.aikido.AikidoAgent.background.utilities.IPCFacilitator.readSocketMessage;
import static dev.aikido.AikidoAgent.background.utilities.IPCFacilitator.stringToBytes;

public class IPCServer {
    private final ServerSocketChannel serverChannel;
    private final CommandRouter commandRouter;
    private final BackgroundProcess process;
    public IPCServer(Path socketPath, BackgroundProcess process) throws IOException, InterruptedException {
        // Delete previous socket file :
        Files.deleteIfExists(socketPath); // Make sure this is alright with multiple agents

        this.process = process;
        commandRouter = new CommandRouter(process.getCloudConnectionManager());
        // Create a new server socket channel :
        UnixDomainSocketAddress socketAddress = UnixDomainSocketAddress.of(socketPath);
        this.serverChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
        serverChannel.bind(socketAddress);
        this.listen();

    }
    private void listen() throws IOException, InterruptedException {
        while (true) {
            SocketChannel channel = serverChannel.accept();
            while (true) {
                if (!serverChannel.isOpen()) {
                    break;
                }
                Optional<String> message = readSocketMessage(channel);
                if (message.isEmpty()) {
                    channel.close();
                    break;
                }
                Optional<String> response = commandRouter.parseIPCInput(message.get());
                if (response.isPresent()) {
                    // Send response :
                    channel.write(stringToBytes(response.get()));
                }
                Thread.sleep(10);
            }
        }
    }
}
