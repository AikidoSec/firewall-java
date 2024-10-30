package dev.aikido.agent_api.background;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.api.events.Heartbeat;
import dev.aikido.agent_api.background.routes.RouteEntry;
import dev.aikido.agent_api.context.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
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
        List<User> users = connectionManager.getUsers().asList();
        // Clear data :
        connectionManager.getRoutes().clear();
        connectionManager.getUsers().clear();

        // Create and send event :
        Heartbeat.HeartbeatEvent event = Heartbeat.get(connectionManager, stats, hostnames, routes, users);
        connectionManager.reportEvent(event, true /* Update config */);
    }
}
