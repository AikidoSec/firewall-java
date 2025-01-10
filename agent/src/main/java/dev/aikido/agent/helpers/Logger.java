package dev.aikido.agent.helpers;

public class Logger {
    public static Logger getLogger() {
        return new Logger();
    }
    public void info(Object obj) {
        System.err.println("[Aikido Agent] " + obj.toString());
    }
    public void debug(Object obj) {
        System.err.println("[Aikido Agent] " + obj.toString());
    }
    public void trace(Object obj) {
        System.err.println("[Aikido Agent] " + obj.toString());
    }
}
