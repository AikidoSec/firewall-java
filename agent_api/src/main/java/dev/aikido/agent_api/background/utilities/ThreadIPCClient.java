package dev.aikido.agent_api.background.utilities;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.Optional;

import static dev.aikido.agent_api.background.utilities.IPCFacilitator.readFromSocket;
import static dev.aikido.agent_api.background.utilities.IPCFacilitator.writeToSocket;

public class ThreadIPCClient {
    private static final Logger logger = LogManager.getLogger(ThreadIPCClient.class);
    private final AFUNIXSocketAddress socketAddress;
    public ThreadIPCClient(File socketFile) {
        try {
            this.socketAddress = AFUNIXSocketAddress.of(socketFile);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }
    public Optional<byte[]> send(byte[] data, boolean receive) {
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
                byte[] response = readFromSocket(socket).get();
                socket.close();
                return Optional.of(response);
            }
        } catch (IOException e) {
            logger.debug("Something went wrong whilst sending data: {}", e.getMessage());
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
