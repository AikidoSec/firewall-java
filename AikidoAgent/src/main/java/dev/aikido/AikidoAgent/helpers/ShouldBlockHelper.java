package dev.aikido.AikidoAgent.helpers;

import com.google.gson.Gson;
import dev.aikido.AikidoAgent.background.ipc_commands.BlockingEnabledCommand;
import dev.aikido.AikidoAgent.background.utilities.IPCDefaultClient;
import dev.aikido.AikidoAgent.helpers.env.BlockingEnv;

import java.util.Optional;

public class ShouldBlockHelper {

    /**
     * Tries to fetch config over IPC, if that fails, uses environment variable
     * @return true if the attack should be blocked
     */
    public static boolean shouldBlock() {
        Optional<String> response = new IPCDefaultClient().sendData(
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
