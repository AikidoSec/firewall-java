package dev.aikido.agent_api.background.cloud.api.events;

import dev.aikido.agent_api.background.cloud.GetManagerInfo;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.User;

import static dev.aikido.agent_api.background.cloud.GetManagerInfo.getManagerInfo;
import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;

public final class CustomEvent {
    private CustomEvent() {}
    public record RequestData(
        // note that URL is not included in the request metadata
        String method,
        String ipAddress,
        String userAgent,
        String source,
        String route
    ) {}

    public record CustomEventEvent(
        String type,
        String name,
        RequestData request,
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

    private static RequestData buildRequestData(ContextObject context) {
        if (context == null) {
            return null;
        }
        return new RequestData(
            context.getMethod(),
            context.getRemoteAddress(),
            context.getHeader("user-agent"),
            context.getSource(),
            context.getRoute()
        );
    }
}
