package dev.aikido.AikidoAgent.background;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class IPCServer {
    private final ServerSocketChannel serverChannel;
    public IPCServer(Path socketPath) throws IOException, InterruptedException {
        // Delete previous socket file :
        Files.deleteIfExists(socketPath); // Make sure this is alright with multiple agents

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
                System.out.printf("[Client message] %s \n", message.get());
                Thread.sleep(10);
            }
        }
    }
    private Optional<String> readSocketMessage(SocketChannel channel) throws IOException {
        // Create 1024 bytes long buffer :
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        // Check if bytes are empty :
        int bytesRead = channel.read(buffer);
        if (bytesRead < 0)
            return Optional.empty();

        // Read bytes :
        byte[] bytes = new byte[bytesRead];
        buffer.flip();
        buffer.get(bytes);

        // Turn bytes into String object :
        String message = new String(bytes);
        return Optional.of(message);
    }
}
