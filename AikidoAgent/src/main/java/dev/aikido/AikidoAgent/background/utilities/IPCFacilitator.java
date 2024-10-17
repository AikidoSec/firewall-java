package dev.aikido.AikidoAgent.background.utilities;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class IPCFacilitator {
    public static ByteBuffer stringToBytes(String str) {
        byte[] stringBytes = str.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(stringBytes.length);
        buffer.put(stringBytes);
        buffer.flip();
        return buffer;
    }
    public static Optional<String> readSocketMessage(SocketChannel channel) throws IOException {
        StringBuilder message  = new StringBuilder();
        // Create 1024 bytes long buffer (This will get re-assigned and read in the while loop)
        ByteBuffer buffer = ByteBuffer.allocate(1024);


        // Read channel until it's empty :
        while (channel.read(buffer) > 0) {
            System.out.println("In while loop reading..");
            buffer.flip();
            byte[] byteArray = new byte[buffer.remaining()];
            buffer.get(byteArray);
            String resultString = new String(byteArray, StandardCharsets.UTF_8);
            message.append(resultString);
            System.out.println(resultString);

            buffer.clear(); // Clear buffer so we can receive new data.
        }
        if (message.toString().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(message.toString());
    }
}
