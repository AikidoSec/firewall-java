package dev.aikido.agent_api.helpers;

import static dev.aikido.agent_api.storage.ConfigStore.getConfig;

public final class ShouldBlockHelper {
    private ShouldBlockHelper() {}

    /**
     * Tries to fetch config over IPC, if that fails, uses environment variable
     * @return true if the attack should be blocked
     */
    public static boolean shouldBlock() {
        return getConfig().isBlockingEnabled();
    }
}
