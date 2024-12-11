package dev.aikido.agent_api.background.utilities;

import dev.aikido.agent_api.helpers.env.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class IPCClientFactory {
    private static final Logger logger = LogManager.getLogger(IPCClientFactory.class);
    private IPCClientFactory() {}

    public static IPCClient getDefaultIPCClient() {
        try {
            Token token = Token.fromEnv();
            return new IPCClient(UDSPath.getUDSPath(token));
        } catch (Error e) {
            logger.debug("Failed to parse token. : {}", e.getMessage());
        }
        return null;
    }
}
