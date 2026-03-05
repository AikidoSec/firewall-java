package dev.aikido.agent_api.context;

import dev.aikido.agent_api.storage.PendingHostnamesStore;

public final class Context {
    private Context() {}

    static final ThreadLocal<ContextObject> threadLocalContext = new ThreadLocal<>();
    public static ContextObject get() {
        return threadLocalContext.get();
    }
    public static void set(ContextObject contextObject) {
        // Flush pending hostnames on every context change to prevent the store from
        // growing unboundedly when a thread is reused across multiple requests.
        PendingHostnamesStore.clear();
        threadLocalContext.set(contextObject);
    }
    public static void reset() {
        threadLocalContext.remove();
    }
}
