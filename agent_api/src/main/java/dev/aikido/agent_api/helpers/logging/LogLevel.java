package dev.aikido.agent_api.helpers.logging;

public enum LogLevel {
    TRACE(0),
    DEBUG(1),
    INFO(2),
    WARN(3),
    ERROR(4),
    FATAL(5);

    private final int level;
    LogLevel(int level) {
        this.level = level;
    }
    public int getLevel() {
        return this.level;
    }
    @Override
    public String toString() {
        return this.name();
    }
}