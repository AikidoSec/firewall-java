package dev.aikido.AikidoAgent.background.utilities;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;

public class IPCClient {
    private final UnixDomainSocketAddress socketAddress;
    public IPCClient(Path socketPath) {
        this.socketAddress = UnixDomainSocketAddress.of(socketPath);
    }
    public void sendData(String data) {
        try {
            // Start a channel :
            SocketChannel channel = SocketChannel
                    .open(StandardProtocolFamily.UNIX);
            channel.connect(socketAddress);

            // Write a message :
            ByteBuffer buffer = stringToBytes(data);
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
            channel.close();
        } catch (IOException ignored) {
            System.out.println("Something went wrong whilst sending data.");
        }
    }
    private static ByteBuffer stringToBytes(String str) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.clear();
        buffer.put(str.getBytes());
        buffer.flip();
        return buffer;
    }
}
