package dev.aikido.AikidoAgent.background.utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;

import static dev.aikido.AikidoAgent.background.utilities.IPCFacilitator.readSocketMessage;
import static dev.aikido.AikidoAgent.background.utilities.IPCFacilitator.stringToBytes;

public class IPCClient {
    private static final Logger logger = LogManager.getLogger(IPCClient.class);
    private final UnixDomainSocketAddress socketAddress;
    public IPCClient(Path socketPath) {
        this.socketAddress = UnixDomainSocketAddress.of(socketPath);
    }
    public void sendData(String data, boolean receive) {
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
            System.out.println("Wrote data to channel.");
            if (receive) {
                System.out.println("Receiving data..");
                Optional<String> response = readSocketMessage(channel);
                System.out.println(response);
            }
            channel.close();
        } catch (IOException e) {
            logger.debug("Something went wrong whilst sending data.");
            logger.trace(e);
        }
    }
}
