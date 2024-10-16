package dev.aikido.AikidoAgent.background.cloud.api.events;

import dev.aikido.AikidoAgent.background.cloud.GetManagerInfo;

import java.util.Map;

public class Attack {
    public record AttackEvent (
        String type,
        RequestData request,
        AttackData attack,
        GetManagerInfo.ManagerInfo agent,
        long time
    ) implements APIEvent {}
    public record RequestData (
        String method,
        Map<String, String[]> headers,
        String ipAddress,
        String userAgent,
        String url,
        String body,
        String source,
        String route
    ) {};
    public record AttackData (
            String kind,
            String operation,
            String module,
            boolean blocked,
            String source,
            String path,
            String stack,
            String payload,
            Map<String, String> metadata
    ) {};
}
