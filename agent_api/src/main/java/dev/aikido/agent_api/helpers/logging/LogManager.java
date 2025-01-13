package dev.aikido.agent_api.helpers.logging;

public class LogManager {
    public static Logger getLogger(Class<?> clazz) {
        return new Logger(clazz);
    }
}
