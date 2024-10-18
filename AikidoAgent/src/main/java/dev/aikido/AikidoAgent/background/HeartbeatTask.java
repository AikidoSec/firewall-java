package dev.aikido.AikidoAgent.background;

import dev.aikido.AikidoAgent.background.cloud.CloudConnectionManager;
import dev.aikido.AikidoAgent.background.cloud.api.events.Heartbeat;
import dev.aikido.AikidoAgent.background.ipc_commands.CommandRouter;
import dev.aikido.AikidoAgent.background.routes.RouteEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.TimerTask;

public class HeartbeatTask extends TimerTask {
    private static final Logger logger = LogManager.getLogger(HeartbeatTask.class);
    private final CloudConnectionManager connectionManager;

    public HeartbeatTask(CloudConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void run() {
        // This gets executed as task (every x seconds)
        logger.debug("Sending out a heartbeat");

        // Get data :
        Object stats = null;
        String[] hostnames = null;
        RouteEntry[] routes = connectionManager.getRoutes().asList();
        String[] users = null;
        // Clear data :
        connectionManager.getRoutes().clear();

        // Create and send event :
        Heartbeat.HeartbeatEvent event = Heartbeat.get(connectionManager, stats, hostnames, routes, users);
        connectionManager.reportEvent(event);
    }
}
