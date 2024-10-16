package dev.aikido.AikidoAgent.background;

import dev.aikido.AikidoAgent.background.cloud.CloudConnectionManager;
import dev.aikido.AikidoAgent.background.utilities.UDSPath;
import dev.aikido.AikidoAgent.helpers.env.Token;

import java.io.IOException;
import java.nio.file.Path;

import static java.lang.Thread.sleep;

public class BackgroundProcess extends Thread {
    private final Token token;
    public BackgroundProcess(String name, Token token) {
        super(name);
        this.token = token;
    }

    public void run() {
        if (!Thread.currentThread().isDaemon() && token == null) {
            return; // Can only run if thread is daemon and token needs to be defined.
        }
        System.out.println("Background thread here!");
        Path socketPath = UDSPath.getUDSPath(token);
        System.out.println("Listening on : " + socketPath);
        try {
            IPCServer server = new IPCServer(socketPath, this);
        } catch (IOException | InterruptedException ignored) {
        }
        System.out.println("Background thread closing.");
    }
}