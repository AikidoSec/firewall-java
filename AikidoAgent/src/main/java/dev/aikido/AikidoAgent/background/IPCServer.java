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

public class IPCServer {
    private final ServerSocketChannel serverChannel;
    private final CommandRouter commandRouter;
    private final BackgroundProcess process;
    public IPCServer(Path socketPath, BackgroundProcess process) throws IOException, InterruptedException {
        // Delete previous socket file :
        Files.deleteIfExists(socketPath); // Make sure this is alright with multiple agents

        this.process = process;
        commandRouter = new CommandRouter();
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
                commandRouter.parseIPCInput(message.get());
                Thread.sleep(10);
            }
        }
    }
    private Optional<String> readSocketMessage(SocketChannel channel) throws IOException {
        StringBuilder message  = new StringBuilder();
        // Create 1024 bytes long buffer :
        ByteBuffer buffer = ByteBuffer.allocate(3);


        // Read channel until it's empty :
        while (channel.read(buffer) > 0) {
            buffer.flip();
            byte[] byteArray = new byte[buffer.remaining()];
            buffer.get(byteArray);
            String resultString = new String(byteArray, StandardCharsets.UTF_8);
            message.append(resultString);

            buffer.clear(); // Clear buffer so we can receive new data.
        }
        if (message.toString().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(message.toString());
    }
}
