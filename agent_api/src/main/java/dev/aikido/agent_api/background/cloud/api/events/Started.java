package dev.aikido.agent_api.background.cloud.api.events;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.GetManagerInfo;

import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;

public class Started {
    public record StartedEvent(
            String type,
            GetManagerInfo.ManagerInfo agent,
            long time
    ) implements APIEvent {};
    public static StartedEvent get(CloudConnectionManager connectionManager) {
        // Get current time :
        return new StartedEvent("started", connectionManager.getManagerInfo(), getUnixTimeMS());
    }
}