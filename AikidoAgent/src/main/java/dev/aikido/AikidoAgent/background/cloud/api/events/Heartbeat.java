package dev.aikido.AikidoAgent.background.cloud.api.events;

import dev.aikido.AikidoAgent.background.cloud.CloudConnectionManager;
import dev.aikido.AikidoAgent.background.cloud.GetManagerInfo;
import dev.aikido.AikidoAgent.background.routes.RouteEntry;

import java.util.Collection;

import static dev.aikido.AikidoAgent.helpers.UnixTimeMS.getUnixTimeMS;

public class Heartbeat {
    public record HeartbeatEvent(
            String type,
            GetManagerInfo.ManagerInfo agent,
            long time,
            Object stats,
            String[] hostnames,
            RouteEntry[] routes,
            String[] users
    ) implements APIEvent {};
    
    public static HeartbeatEvent get(
            CloudConnectionManager connectionManager,
            Object stats, String[] hostnames, RouteEntry[] routes, String[] users
    ) {
        long time = getUnixTimeMS(); // Get current time
        GetManagerInfo.ManagerInfo agent = connectionManager.getManagerInfo();
        return new HeartbeatEvent("heartbeat", agent, time, stats, hostnames, routes, users);
    }
}