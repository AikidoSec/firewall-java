package dev.aikido.agent_api.background.cloud.api.events;

import static dev.aikido.agent_api.background.cloud.GetManagerInfo.getManagerInfo;
import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;

import dev.aikido.agent_api.background.cloud.GetManagerInfo;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.User;

public final class CustomEvent {
    private CustomEvent() {}

    public record RequestData(
            // note that URL is not included in the request metadata
            String method, String ipAddress, String userAgent, String source, String route) {}

    public record UserData(String id, String name) {}

    public record CustomEventEvent(
            String type, String name, RequestData request, GetManagerInfo.ManagerInfo agent, UserData user, long time)
            implements APIEvent {}

    public static CustomEventEvent createAPIEvent(String eventName, ContextObject context) {
        return new CustomEventEvent(
                "custom",
                eventName,
                buildRequestData(context),
                getManagerInfo(),
                buildUserData(context),
                getUnixTimeMS());
    }

    private static UserData buildUserData(ContextObject context) {
        if (context.getUser() == null) {
            return null;
        }
        User user = context.getUser();
        return new UserData(user.id(), user.name());
    }

    private static RequestData buildRequestData(ContextObject context) {
        return new RequestData(
                context.getMethod(),
                context.getRemoteAddress(),
                context.getHeader("user-agent"),
                context.getSource(),
                context.getRoute());
    }
}
