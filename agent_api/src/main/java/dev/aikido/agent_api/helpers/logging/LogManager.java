package dev.aikido.agent_api.helpers.logging;

public final class LogManager {
    private LogManager() {}
    public static Logger getLogger(Class<?> clazz) {
        if (clazz != null) {
            return new Logger(clazz);
        }
        return null;
    }
}
