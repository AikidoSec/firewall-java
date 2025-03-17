package dev.aikido.agent_api.background;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.helpers.env.BlockingEnv;
import dev.aikido.agent_api.helpers.env.Token;

import java.util.Timer;

import static dev.aikido.agent_api.Config.heartbeatEveryXSeconds;
import static dev.aikido.agent_api.Config.pollingEveryXSeconds;

public class BackgroundProcess extends Thread {
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
        // Create a cloud-connection manager:
        this.connectionManager = new CloudConnectionManager(new BlockingEnv().getValue(), token, null);
        this.connectionManager.onStart();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(
                new HeartbeatTask(connectionManager), // Heartbeat task: Sends statistics, route data, etc.
                heartbeatEveryXSeconds * 1000, // Delay before first execution in milliseconds
                heartbeatEveryXSeconds * 1000 // Interval in milliseconds
        );
        timer.scheduleAtFixedRate(
                new RealtimeTask(connectionManager), // Realtime task: makes sure config updates happen fast
                pollingEveryXSeconds * 1000, // Delay before first execution in milliseconds
                pollingEveryXSeconds * 1000 // Interval in milliseconds
        );
        timer.scheduleAtFixedRate(
                new AttackQueueConsumerTask(connectionManager), // Consumes from the attack queue (so attacks are reported in background)
                /* delay: */ 0, /* interval: */ 2 * 1000 // Clear queue every 2 seconds
        );
        // Report initial statistics if those were not received
        timer.schedule(
                new HeartbeatTask(connectionManager, true /* Check for initial statistics */), // Initial heartbeat task
                60_000 // Delay in ms
        );
    }

    public CloudConnectionManager getCloudConnectionManager() {
        return connectionManager;
    }
}