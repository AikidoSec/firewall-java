package dev.aikido.agent_api.background.utilities;

import dev.aikido.agent_api.helpers.env.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class IPCClientFactory {
    private static final Logger logger = LogManager.getLogger(IPCClientFactory.class);
    private IPCClientFactory() {}

    public static IPCClient getDefaultIPCClient() {
        Token token = Token.fromEnv();
        if (token == null) {
            logger.debug("Invalid token");
        }
        return new IPCClient(UDSPath.getUDSPath(token));
    }
}
