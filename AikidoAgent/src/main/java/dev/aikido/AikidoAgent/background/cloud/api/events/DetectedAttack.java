package dev.aikido.AikidoAgent.background.cloud.api.events;

import dev.aikido.AikidoAgent.background.cloud.CloudConnectionManager;
import dev.aikido.AikidoAgent.background.cloud.GetManagerInfo;
import dev.aikido.AikidoAgent.context.ContextObject;
import dev.aikido.AikidoAgent.vulnerabilities.Attack;

import java.util.HashMap;
import java.util.Map;

import static dev.aikido.AikidoAgent.helpers.UnixTimeMS.getUnixTimeMS;
import static dev.aikido.AikidoAgent.helpers.extraction.UserAgentFromContext.getUserAgent;

public class DetectedAttack {
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
            "MODULE?", connectionManager.shouldBlock(), "stack?"
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
