package dev.aikido.agent_api;

public abstract class Config {
    public static final String pkgVersion = "0.1.0";

    public static final int heartbeatEveryXSeconds = 600; // 10 minutes
    public static final int pollingEveryXSeconds = 60; // Check for realtime config changes every 1 minute
}
