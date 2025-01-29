package dev.aikido.agent_api.helpers;

import java.time.Instant;

public final class UnixTimeMS {
    private UnixTimeMS() {
    }

    public static long getUnixTimeMS() {
        return Instant.now().toEpochMilli();
    }
}
