package dev.aikido.agent_api.background.utilities;

import dev.aikido.agent_api.helpers.env.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public final class ThreadClientFactory {
    private static final Logger logger = LogManager.getLogger(ThreadClientFactory.class);
    private ThreadClientFactory() {}

    public static ThreadClient getDefaultThreadClient() {
        Token token = Token.fromEnv();
        if (token == null) {
            logger.debug("Invalid token");
            return null;
        }
        return new ThreadClient(UDSPath.getUDSPath(token));
    }
}
