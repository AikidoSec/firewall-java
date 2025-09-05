package dev.aikido.agent_api.background.cloud.api.events;

import dev.aikido.agent_api.background.cloud.GetManagerInfo;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.User;

import java.util.Map;

import static dev.aikido.agent_api.background.cloud.GetManagerInfo.getManagerInfo;
import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;
import static dev.aikido.agent_api.storage.ServiceConfigStore.getConfig;

public final class DetectedAttackWave {
    private DetectedAttackWave() {
    }

    public record DetectedAttackWaveEvent(
        String type,
        RequestData request,
        AttackWaveData attack,
        GetManagerInfo.ManagerInfo agent,
        long time
    ) implements APIEvent {
    }

    public record RequestData(
        String ipAddress,
        String userAgent,
        String source
    ) {
    }

    public record AttackWaveData(
        Map<String, String> metadata,
        User user
    ) {
    }

    public static DetectedAttackWaveEvent createAPIEvent(ContextObject context) {
        RequestData requestData = new RequestData(
            context.getRemoteAddress(), // ipAddress
            context.getHeader("user-agent"), // userAgent
            context.getSource() // source
        );
        AttackWaveData attackData = new AttackWaveData(
            Map.of(),
            context.getUser()
        );

        return new DetectedAttackWaveEvent(
            "detected_attack_wave", // type
            requestData, // request
            attackData, // attack
            getManagerInfo(), // agent
            getUnixTimeMS() // time
        );
    }
}
