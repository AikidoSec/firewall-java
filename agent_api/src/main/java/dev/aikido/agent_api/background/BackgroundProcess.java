package dev.aikido.agent_api.background;

import static dev.aikido.agent_api.Config.heartbeatEveryXSeconds;
import static dev.aikido.agent_api.Config.pollingEveryXSeconds;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.background.utilities.UDSPath;
import dev.aikido.agent_api.helpers.env.BlockingEnv;
import dev.aikido.agent_api.helpers.env.Token;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BackgroundProcess extends Thread {
    private static final Logger logger = LogManager.getLogger(BackgroundProcess.class);
    private CloudConnectionManager connectionManager;
    private final Token token;
    private BlockingQueue<APIEvent> attackQueue;

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
        // Create a queue and a thread to handle attacks that need reporting in the background:
        this.attackQueue = new LinkedBlockingQueue<>();
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
                new AttackQueueConsumerTask(
                        connectionManager,
                        attackQueue), // Consumes from the attack queue (so attacks are reported in background)
                /* delay: */ 0, /* interval: */
                2 * 1000 // Clear queue every 2 seconds
                );
        // Report initial statistics if those were not received
        timer.schedule(
                new HeartbeatTask(connectionManager, true /* Check for initial statistics */), // Initial heartbeat task
                60_000 // Delay in ms
                );
        try {
            File queueDir = UDSPath.getUDSPath(token);
            if (!queueDir.getParentFile().canWrite()) {
                logger.error("AIKIDO: Cannot write to socket %s, please verify access", queueDir.getPath());
            }
            BackgroundReceiver server = new BackgroundReceiver(queueDir, this);
        } catch (IOException | InterruptedException e) {
            logger.trace(e);
        }
        logger.debug("Background thread closing.");
    }

    public CloudConnectionManager getCloudConnectionManager() {
        return connectionManager;
    }

    public BlockingQueue<APIEvent> getAttackQueue() {
        return attackQueue;
    }
}
