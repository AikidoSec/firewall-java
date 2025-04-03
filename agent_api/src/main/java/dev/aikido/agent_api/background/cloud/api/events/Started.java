package dev.aikido.agent_api.background.cloud.api.events;

import dev.aikido.agent_api.background.cloud.GetManagerInfo;

import static dev.aikido.agent_api.background.cloud.GetManagerInfo.getManagerInfo;
import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;

public final class Started {
    private Started() {}
    public record StartedEvent(
            String type,
            GetManagerInfo.ManagerInfo agent,
            long time
    ) implements APIEvent {};
    public static StartedEvent get() {
        // Get current time :
        return new StartedEvent("started", getManagerInfo(), getUnixTimeMS());
    }
}