package dev.aikido.agent_api.background.cloud.api.events;

import dev.aikido.agent_api.background.cloud.GetManagerInfo;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.User;

import static dev.aikido.agent_api.background.cloud.GetManagerInfo.getManagerInfo;
import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;

public final class CustomEvent {
    private CustomEvent() {}
    public record CustomEventEvent(
        String type,
        String name,
        DetectedAttack.RequestData request,
        GetManagerInfo.ManagerInfo agent,
        User user,
        long time
    ) implements APIEvent {}

    public static CustomEventEvent createAPIEvent(String eventName, ContextObject context) {
        return new CustomEventEvent(
            "custom", // type
            eventName, // name
            buildRequestData(context), // request
            getManagerInfo(), // agent
            context != null ? context.getUser() : null, // user
            getUnixTimeMS() // time
        );
    }

    private static DetectedAttack.RequestData buildRequestData(ContextObject context) {
        if (context == null) {
            return null;
        }
        return new DetectedAttack.RequestData(
            context.getMethod(),
            context.getRemoteAddress(),
            context.getHeader("user-agent"),
            context.getUrl(),
            context.getSource(),
            context.getRoute()
        );
    }
}
