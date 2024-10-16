package dev.aikido.AikidoAgent.background.cloud.api.events;

import dev.aikido.AikidoAgent.background.cloud.CloudConnectionManager;
import dev.aikido.AikidoAgent.background.cloud.GetManagerInfo;

import java.time.Instant;

import static dev.aikido.AikidoAgent.helpers.UnixTimeMS.getUnixTimeMS;

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