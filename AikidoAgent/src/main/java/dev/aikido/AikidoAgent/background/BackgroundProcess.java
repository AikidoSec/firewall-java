package dev.aikido.AikidoAgent.background;

import dev.aikido.AikidoAgent.background.utilities.UDSPath;

import java.io.IOException;
import java.nio.file.Path;

import static java.lang.Thread.sleep;

public class BackgroundProcess extends Thread {
    public BackgroundProcess(String name) {
        super(name);
    }

    public void run() {
        if (!Thread.currentThread().isDaemon()) {
            return; // Can only run if thread is daemon
        }
        System.out.println("Background thread here!");
        Path socketPath = UDSPath.getUDSPath();
        try {
            IPCServer server = new IPCServer(socketPath);
        } catch (IOException | InterruptedException ignored) {
        }
        System.out.println("Background thread closing.");
    }
}