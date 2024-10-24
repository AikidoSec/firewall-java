package dev.aikido.agent_api.background.utilities;

import org.newsclub.net.unix.AFUNIXSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class IPCFacilitator {
    private static final int BufferSize = 1024;
    public static Optional<String> readFromSocket(AFUNIXSocket socket) {
        try {
            InputStream input = socket.getInputStream();

            StringBuilder message  = new StringBuilder();
            int bytesRead;
            byte[] buffer = new byte[BufferSize];
            while ((bytesRead = input.read(buffer)) > 0) {
                String resultString = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                message.append(resultString);
                buffer = new byte[BufferSize]; // Re-initialize buffer
                if (bytesRead < BufferSize) {
                    // No more bytes left to read, break loop
                    break;
                }
            }
            if (!message.toString().isEmpty()) {
                return Optional.of(message.toString());
            }
        } catch (IOException ignored) {
        }
        return Optional.empty();
    }
    public static void writeToSocket(AFUNIXSocket socket, String message) {
        try {
            OutputStream output = socket.getOutputStream();
            if (message != null && !message.isEmpty()) {
                output.write(message.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException ignored) {
        }
    }
}
