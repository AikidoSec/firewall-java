package dev.aikido.agent_api.context;

import dev.aikido.agent_api.storage.PendingHostnamesStore;

public final class Context {
    private Context() {}

    static final ThreadLocal<ContextObject> threadLocalContext = new ThreadLocal<>();
    // Bypassed requests have no context, so this flag tells them apart from code running outside a request.
    static final ThreadLocal<Boolean> threadLocalBypassed = new ThreadLocal<>();
    public static ContextObject get() {
        return threadLocalContext.get();
    }
    public static void set(ContextObject contextObject) {
        threadLocalContext.set(contextObject);
    }
    public static void reset() {
        threadLocalContext.remove();
        threadLocalBypassed.remove();
    }
    public static void markBypassed() {
        threadLocalBypassed.set(true);
    }
    public static boolean isBypassed() {
        return threadLocalBypassed.get() != null;
    }
}
