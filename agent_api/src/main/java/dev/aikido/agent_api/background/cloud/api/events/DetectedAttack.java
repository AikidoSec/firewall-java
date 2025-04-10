package dev.aikido.agent_api.background.cloud.api.events;

import dev.aikido.agent_api.background.cloud.GetManagerInfo;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.User;
import dev.aikido.agent_api.vulnerabilities.Attack;

import java.util.List;
import java.util.Map;

import static dev.aikido.agent_api.background.cloud.GetManagerInfo.getManagerInfo;
import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;
import static dev.aikido.agent_api.storage.ServiceConfigStore.getConfig;

public final class DetectedAttack {
    private DetectedAttack() {}
    public record DetectedAttackEvent (
        String type,
        RequestData request,
        AttackData attack,
        GetManagerInfo.ManagerInfo agent,
        long time
    ) implements APIEvent {}
    public record RequestData (
        String method,
        Map<String, List<String>> headers,
        String ipAddress,
        String userAgent,
        String url,
        String body,
        String source,
        String route
    ) {};
    public record AttackData (
            // Data gathered from Attack class :
            String kind,
            String operation,
            String source,
            String path,
            String payload,
            Map<String, String> metadata,
            // Auxiliary attack data :
            String module,
            boolean blocked,
            String stack,
            User user
    ) {};

    public static DetectedAttackEvent createAPIEvent(Attack attack, ContextObject context) {
        boolean blocking = getConfig().isBlockingEnabled();
        RequestData requestData = new RequestData(
            context.getMethod(), // Method
            context.getHeaders(), // headers
            context.getRemoteAddress(), // ipAddress
            context.getHeader("user-agent"), // userAgent
            context.getUrl(), // url
            context.getJSONBody(), // body
            context.getSource(), // source
            context.getRoute() // route
        );
        AttackData attackData = new AttackData(
            attack.kind, attack.operation, attack.source, attack.pathToPayload, attack.payload, attack.metadata,
            "module", blocking, attack.stack, attack.user
        );
        return new DetectedAttackEvent(
        "detected_attack", // type
            requestData, // request
            attackData, // attack
            getManagerInfo(), // agent
            getUnixTimeMS() // time
        );
    }
}
