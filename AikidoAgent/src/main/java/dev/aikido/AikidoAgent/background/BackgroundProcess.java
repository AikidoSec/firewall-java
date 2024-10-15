package dev.aikido.AikidoAgent.background;

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
        Path socketPath = Path
            .of(System.getProperty("user.home"))
            .resolve("aikido2342.socket");
        try {
            IPCServer server = new IPCServer(socketPath);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            this.start(); // Restart thread
        }
    }
}