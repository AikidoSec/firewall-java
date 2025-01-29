package dev.aikido.agent_api.helpers;

import static dev.aikido.agent_api.background.utilities.ThreadIPCClientFactory.getDefaultThreadIPCClient;

import dev.aikido.agent_api.background.ipc_commands.BlockingEnabledCommand;
import dev.aikido.agent_api.background.ipc_commands.Command;
import dev.aikido.agent_api.background.utilities.ThreadIPCClient;
import dev.aikido.agent_api.helpers.env.BlockingEnv;
import java.util.Optional;

public final class ShouldBlockHelper {
    private ShouldBlockHelper() {}

    /**
     * Tries to fetch config over IPC, if that fails, uses environment variable
     * @return true if the attack should be blocked
     */
    public static boolean shouldBlock() {
        ThreadIPCClient client = getDefaultThreadIPCClient();
        if (client == null) {
            // Fallback on environment variable :
            return new BlockingEnv().getValue();
        }
        Optional<BlockingEnabledCommand.Res> res = new BlockingEnabledCommand().send(client, new Command.EmptyResult());
        if (!res.isEmpty()) {
            return res.get().isBlockingEnabled();
        }

        // Fallback on environment variable :
        return new BlockingEnv().getValue();
    }
}
