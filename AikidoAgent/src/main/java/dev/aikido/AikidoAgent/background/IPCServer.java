package dev.aikido.AikidoAgent.background;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Optional;

public class IPCServer {
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
