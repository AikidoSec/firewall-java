package dev.aikido.agent_api.background;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.utilities.UDSPath;
import dev.aikido.agent_api.helpers.env.BlockingEnv;
import dev.aikido.agent_api.helpers.env.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

public class BackgroundProcess extends Thread {
    private static final Logger logger = LogManager.getLogger(BackgroundProcess.class);
    private CloudConnectionManager connectionManager;
    private final Token token;
    public BackgroundProcess(String name, Token token) {
        super(name);
        this.token = token;
    }

    public void run() {
        if (!Thread.currentThread().isDaemon() && token == null) {
            return; // Can only run if thread is daemon and token needs to be defined.
        }
        Path socketPath = UDSPath.getUDSPath(token);
        logger.debug("Background Process started, Listening on : {}", socketPath);
        this.connectionManager = new CloudConnectionManager(new BlockingEnv().getValue(), token, null);
        this.connectionManager.onStart();
        try {
            IPCServer server = new IPCServer(socketPath, this);
        } catch (IOException | InterruptedException ignored) {
        }
        logger.debug("Background thread closing.");
    }
    public CloudConnectionManager getCloudConnectionManager() {
        return connectionManager;
    }
}