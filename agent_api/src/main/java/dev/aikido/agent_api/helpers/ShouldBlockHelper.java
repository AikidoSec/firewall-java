package dev.aikido.agent_api.helpers;


import dev.aikido.agent_api.storage.ServiceConfigStore;

public final class ShouldBlockHelper {
    private ShouldBlockHelper() {}

    /**
     * Fetches blocking variable from config
     * @return true if the attack should be blocked
     */
    public static boolean shouldBlock() {
        return ServiceConfigStore.getConfig().isBlockingEnabled();
    }
}
