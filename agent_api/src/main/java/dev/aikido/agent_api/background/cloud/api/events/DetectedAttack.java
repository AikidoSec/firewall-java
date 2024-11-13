package dev.aikido.agent_api.background.cloud.api.events;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.GetManagerInfo;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.vulnerabilities.Attack;

import java.util.Map;

import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;
import static dev.aikido.agent_api.helpers.extraction.UserAgentFromContext.getUserAgent;

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
        Map<String, String> headers,
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
            String stack
    ) {};

    public static DetectedAttackEvent createAPIEvent(Attack attack, ContextObject context, CloudConnectionManager connectionManager) {
        RequestData requestData = new RequestData(
            context.getMethod(), // Method
            context.getHeaders(), // headers
            context.getRemoteAddress(), // ipAddress
            getUserAgent(context), // userAgent
            context.getUrl(), // url
            context.getJSONBody(), // body
            context.getSource(), // source
            context.getRoute() // route
        );
        AttackData attackData = new AttackData(
            attack.kind, attack.operation, attack.source, attack.pathToPayload, attack.payload, attack.metadata,
            "MODULE?", connectionManager.shouldBlock(), attack.stack
        );
        return new DetectedAttackEvent(
        "detected_attack", // type
            requestData, // request
            attackData, // attack
            connectionManager.getManagerInfo(), // agent
            getUnixTimeMS() // time
        );
    }
}
