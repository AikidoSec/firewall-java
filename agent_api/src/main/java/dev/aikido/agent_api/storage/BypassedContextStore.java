package dev.aikido.agent_api.storage;

/**
 * Thread-local flag recording whether the current request's remote IP is in the bypass list.
 * Needed because bypassed requests intentionally do not set a context, but for Stored SSRF we still want to skip.
 */
public final class BypassedContextStore {
    private BypassedContextStore() {}

    private static final ThreadLocal<Boolean> store = ThreadLocal.withInitial(() -> false);

    public static void setBypassed(boolean bypassed) {
        store.set(bypassed);
    }

    public static boolean isBypassed() {
        return store.get();
    }

    public static void clear() {
        store.remove();
    }
}
