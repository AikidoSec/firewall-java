package dev.aikido.AikidoAgent.background;

public class BackgroundProcess extends Thread {
    public BackgroundProcess(String name) {
        super(name);
    }

    public void run() {
            if (!Thread.currentThread().isDaemon()) {
            return; // Can only run if thread is daemon
        }
        System.out.println("Background thread here!");
    }
}