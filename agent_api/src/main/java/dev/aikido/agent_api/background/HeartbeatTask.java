package dev.aikido.agent_api.background;

import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApiHTTP;
import dev.aikido.agent_api.background.cloud.api.events.Heartbeat;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import dev.aikido.agent_api.storage.*;
import dev.aikido.agent_api.storage.routes.RouteEntry;
import dev.aikido.agent_api.context.User;
import dev.aikido.agent_api.storage.routes.RoutesStore;

import java.util.List;
import java.util.Optional;
import java.util.TimerTask;

public class HeartbeatTask extends TimerTask {
    private static final Logger logger = LogManager.getLogger(HeartbeatTask.class);
    private final ReportingApiHTTP api;
    private final boolean shouldCheckForInitialStats;

    public HeartbeatTask(ReportingApiHTTP api) {
        this(api, false);
    }
    public HeartbeatTask(ReportingApiHTTP api, boolean shouldCheckForInitialStats) {
        this.api = api;
        this.shouldCheckForInitialStats = shouldCheckForInitialStats;
    }

    @Override
    public void run() {
        if (shouldCheckForInitialStats && ServiceConfigStore.getConfig().hasReceivedAnyStats()) {
            // Stats were already sent, so return :
            return;
        }
        // This gets executed as task (every x seconds)
        logger.debug("Sending out a heartbeat");

        // Get data :
        Statistics.StatsRecord stats = StatisticsStore.getStatsRecord();
        Hostnames.HostnameEntry[] hostnames = HostnamesStore.getHostnamesAsList();
        RouteEntry[] routes = RoutesStore.getRoutesAsList();
        List<User> users = UsersStore.getUsersAsList();

        // Clear data :
        StatisticsStore.clear();
        HostnamesStore.clear();
        RoutesStore.clear();
        UsersStore.clear();

        // Create and send event :
        Heartbeat.HeartbeatEvent event = Heartbeat.get(stats, hostnames, routes, users);
        Optional<APIResponse> res = api.report(event);
        res.ifPresent(ServiceConfigStore::updateFromAPIResponse);
    }
}
