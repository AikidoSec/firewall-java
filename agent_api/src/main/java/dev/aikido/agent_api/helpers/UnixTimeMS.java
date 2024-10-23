package dev.aikido.agent_api.helpers;

import java.time.Instant;

public class UnixTimeMS {
    public static long getUnixTimeMS() {
        return Instant.now().getEpochSecond() * 1000;
    }
}
