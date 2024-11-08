package dev.aikido.agent_api.background.cloud.api.events;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.GetManagerInfo;
import dev.aikido.agent_api.storage.routes.RouteEntry;
import dev.aikido.agent_api.context.User;

import java.util.List;

import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;

public class Heartbeat {
    public record HeartbeatEvent(
            String type,
            GetManagerInfo.ManagerInfo agent,
            long time,
            Object stats,
            String[] hostnames,
            RouteEntry[] routes,
            List<User> users
    ) implements APIEvent {};
    
    public static HeartbeatEvent get(
            CloudConnectionManager connectionManager,
            Object stats, String[] hostnames, RouteEntry[] routes, List<User> users
    ) {
        long time = getUnixTimeMS(); // Get current time
        GetManagerInfo.ManagerInfo agent = connectionManager.getManagerInfo();
        return new HeartbeatEvent("heartbeat", agent, time, stats, hostnames, routes, users);
    }
}