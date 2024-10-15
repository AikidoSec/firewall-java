package dev.aikido.AikidoAgent.background.cloud.api.events;

import dev.aikido.AikidoAgent.background.cloud.CloudConnectionManager;
import dev.aikido.AikidoAgent.background.cloud.GetManagerInfo;

import java.time.Instant;

public class Started {
    public record StartedEvent(
            String type,
            GetManagerInfo.ManagerInfo agent,
            long time
    ) implements APIEvent {};
    public static StartedEvent get(CloudConnectionManager connectionManager) {
        // Get current time :
        long time = Instant.now().getEpochSecond() * 1000;
        return new StartedEvent("started", connectionManager.getManagerInfo(), time);
    }
}