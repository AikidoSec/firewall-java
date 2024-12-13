package dev.aikido.agent_api.helpers;

import dev.aikido.agent_api.background.utilities.ThreadClient;
import dev.aikido.agent_api.background.ipc_commands.BlockingEnabledCommand;
import dev.aikido.agent_api.helpers.env.BlockingEnv;

import static dev.aikido.agent_api.background.utilities.ThreadClientFactory.getDefaultThreadClient;


public final class ShouldBlockHelper {
    private ShouldBlockHelper() {}

    /**
     * Tries to fetch config over IPC, if that fails, uses environment variable
     * @return true if the attack should be blocked
     */
    public static boolean shouldBlock() {
        ThreadClient client = getDefaultThreadClient();
        if (client == null) {
            // Fallback on environment variable :
            return new BlockingEnv().getValue();
        }
        Optional<String> response = client.sendData(
                "BLOCKING_ENABLED$", // data
                true // receives a response
        );
        if (response.isPresent()) {
            Gson gson = new Gson();
            BlockingEnabledCommand.BlockingEnabledResult res = gson.fromJson(response.get(), BlockingEnabledCommand.BlockingEnabledResult.class);
            if (res != null) {
                return res.isBlockingEnabled();
            }
        }
        // Fallback on environment variable :
        return new BlockingEnv().getValue();
    }
}
