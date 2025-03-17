package dev.aikido.agent_api.background;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.api.events.Heartbeat;
import dev.aikido.agent_api.background.users.UsersStore;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import dev.aikido.agent_api.storage.Hostnames;
import dev.aikido.agent_api.storage.HostnamesStore;
import dev.aikido.agent_api.storage.Statistics;
import dev.aikido.agent_api.storage.StatisticsStore;
import dev.aikido.agent_api.storage.routes.RouteEntry;
import dev.aikido.agent_api.context.User;
import dev.aikido.agent_api.storage.routes.RoutesStore;

import java.util.List;
import java.util.TimerTask;

import static dev.aikido.agent_api.storage.ConfigStore.getConfig;
import static dev.aikido.agent_api.storage.StatisticsStore.getStatsRecord;

public class HeartbeatTask extends TimerTask {
    private static final Logger logger = LogManager.getLogger(HeartbeatTask.class);
    private final CloudConnectionManager connectionManager;
    private final boolean shouldCheckForInitialStats;

    public HeartbeatTask(CloudConnectionManager connectionManager) {
        this(connectionManager, false);
    }
    public HeartbeatTask(CloudConnectionManager connectionManager, boolean shouldCheckForInitialStats) {
        this.connectionManager = connectionManager;
        this.shouldCheckForInitialStats = shouldCheckForInitialStats;
    }

    @Override
    public void run() {
        if (shouldCheckForInitialStats && getConfig().hasReceivedAnyStats()) {
            // Stats were already sent, so return :
            return;
        }
        // This gets executed as task (every x seconds)
        logger.debug("Sending out a heartbeat");

        // Get data :
        Statistics.StatsRecord stats = getStatsRecord();
        Hostnames.HostnameEntry[] hostnames = HostnamesStore.getHostnamesAsList();
        RouteEntry[] routes = RoutesStore.getRoutesAsList();
        List<User> users = UsersStore.getUsersAsList();
        // Clear data :
        RoutesStore.clear();
        UsersStore.clear();
        StatisticsStore.clear();
        HostnamesStore.clear();

        // Create and send event :
        Heartbeat.HeartbeatEvent event = Heartbeat.get(stats, hostnames, routes, users);
        connectionManager.reportEvent(event, true /* Update config */);
    }
}
