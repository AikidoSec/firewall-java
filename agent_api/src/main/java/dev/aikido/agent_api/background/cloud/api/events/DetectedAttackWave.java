package dev.aikido.agent_api.background.cloud.api.events;

import com.google.gson.Gson;
import dev.aikido.agent_api.background.cloud.GetManagerInfo;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.User;
import dev.aikido.agent_api.storage.attack_wave_detector.AttackWaveDetector;
import dev.aikido.agent_api.storage.attack_wave_detector.AttackWaveDetectorStore;

import java.util.List;
import java.util.Map;

import static dev.aikido.agent_api.background.cloud.GetManagerInfo.getManagerInfo;
import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;

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

        String ip = context.getRemoteAddress();
        List<AttackWaveDetector.Sample> samples = AttackWaveDetectorStore.getSamplesForIp(ip);
        Map<String, String> metadata = Map.of(
            "samples", new Gson().toJson(samples)
        );

        AttackWaveData attackData = new AttackWaveData(
            metadata, context.getUser()
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
