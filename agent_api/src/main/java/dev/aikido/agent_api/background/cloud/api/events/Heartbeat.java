package dev.aikido.agent_api.background.cloud.api.events;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.GetManagerInfo;
import dev.aikido.agent_api.storage.Hostnames;
import dev.aikido.agent_api.storage.Statistics;
import dev.aikido.agent_api.storage.routes.RouteEntry;
import dev.aikido.agent_api.context.User;

import java.util.List;

import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;

public final class Heartbeat {
    private Heartbeat() {}
    public record HeartbeatEvent(
            String type,
            GetManagerInfo.ManagerInfo agent,
            long time,
            Statistics.StatsRecord stats,
            Hostnames.HostnameEntry[] hostnames,
            RouteEntry[] routes,
            List<User> users,
            boolean middlewareInstalled
    ) implements APIEvent {}
    
    public static HeartbeatEvent get(
            CloudConnectionManager connectionManager,
            Statistics.StatsRecord stats, Hostnames.HostnameEntry[] hostnames, RouteEntry[] routes, List<User> users
    ) {
        long time = getUnixTimeMS(); // Get current time
        GetManagerInfo.ManagerInfo agent = connectionManager.getManagerInfo();
        boolean middlewareInstalled = connectionManager.getConfig().isMiddlewareInstalled();
        return new HeartbeatEvent("heartbeat", agent, time, stats, hostnames, routes, users, middlewareInstalled);
    }
}