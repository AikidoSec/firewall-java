package dev.aikido.agent_api;

public final class Config {
    private Config() {
    }

    public static final String pkgVersion = "1.0-REPLACE-VERSION";

    public static final int heartbeatEveryXSeconds = 600; // 10 minutes
    public static final int pollingEveryXSeconds = 60; // Check for realtime config changes every 1 minute
}
