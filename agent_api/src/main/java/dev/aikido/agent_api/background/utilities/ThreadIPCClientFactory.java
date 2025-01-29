package dev.aikido.agent_api.background.utilities;

import dev.aikido.agent_api.helpers.env.Token;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

public final class ThreadIPCClientFactory {
    private static final Logger logger = LogManager.getLogger(ThreadIPCClientFactory.class);

    private ThreadIPCClientFactory() {
    }

    public static ThreadIPCClient getDefaultThreadIPCClient() {
        Token token = Token.fromEnv();
        if (token == null) {
            logger.debug("Invalid token");
            return null;
        }
        return new ThreadIPCClient(UDSPath.getUDSPath(token));
    }
}
