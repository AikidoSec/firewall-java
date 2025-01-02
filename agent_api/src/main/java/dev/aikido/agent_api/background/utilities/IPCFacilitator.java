package dev.aikido.agent_api.background.utilities;

import org.newsclub.net.unix.AFUNIXSocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public final class IPCFacilitator {
    private IPCFacilitator() {}
    private static final int BufferSize = 1024;
    public static Optional<byte[]> readFromSocket(AFUNIXSocket socket) {
        try {
            InputStream input = socket.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int bytesRead;
            byte[] buffer = new byte[BufferSize];
            while ((bytesRead = input.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
                buffer = new byte[BufferSize]; // Re-initialize buffer
                if (bytesRead < BufferSize) {
                    // No more bytes left to read, break loop
                    break;
                }
            }
            byte[] rawBytes = byteArrayOutputStream.toByteArray();
            return Optional.of(rawBytes);
        } catch (IOException ignored) {
        }
        return Optional.empty();
    }
    public static void writeToSocket(AFUNIXSocket socket, byte[] message) {
        try {
            OutputStream output = socket.getOutputStream();
            if (message != null && message.length > 0) {
                output.write(message);
            }
        } catch (IOException ignored) {
        }
    }
}