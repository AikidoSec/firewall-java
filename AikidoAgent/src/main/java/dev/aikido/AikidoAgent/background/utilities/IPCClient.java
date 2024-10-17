package dev.aikido.AikidoAgent.background.utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

import java.io.IOException;
import java.net.SocketException;
import java.nio.file.Path;
import java.util.Optional;

import static dev.aikido.AikidoAgent.background.utilities.IPCFacilitator.*;

public class IPCClient {
    private static final Logger logger = LogManager.getLogger(IPCClient.class);
    private final AFUNIXSocketAddress socketAddress;
    public IPCClient(Path socketPath) {
        try {
            this.socketAddress = AFUNIXSocketAddress.of(socketPath.toFile());
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }
    public Optional<String> sendData(String data, boolean receive) {
        AFUNIXSocket socket = null;
        try {
            // Start socket
            socket = AFUNIXSocket.newInstance();
            socket.connect(socketAddress);

            // Write a message :
            if (socket.isConnected()) {
                writeToSocket(socket, data);
            }
            if (receive && socket.isConnected()) {
                Optional<String> response = readFromSocket(socket);
                socket.close();
                return response;
            }
        } catch (IOException e) {
            logger.debug("Something went wrong whilst sending data.");
            logger.trace(e);
        } finally {
            // Make sure the socket is also closed in event of a crash :
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
            }
        }
        return Optional.empty();
    }
}
