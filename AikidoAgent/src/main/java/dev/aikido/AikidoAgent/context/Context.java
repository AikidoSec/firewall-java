package dev.aikido.AikidoAgent.context;

public class Context {
    static final ThreadLocal<ContextObject> threadLocalContext = new ThreadLocal<>();
    public static ContextObject get() {
        return threadLocalContext.get();
    }
    public static void set(ContextObject contextObject) {
        threadLocalContext.set(contextObject);
    }
    public static void reset() {
        threadLocalContext.remove();
    }
}
