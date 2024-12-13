package dev.aikido.agent_api.background;

import dev.aikido.agent_api.helpers.env.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Starter {
    private static final Logger logger = LogManager.getLogger(Starter.class);
    public static void start() {
        Token token = Token.fromEnv();
        if (token == null) {
            logger.info("Failed to start background process due to an invalid token");
            return;
        }
        BackgroundProcess backgroundProcess = new BackgroundProcess("main-background-process", token);
        backgroundProcess.setDaemon(true);
        backgroundProcess.start();
    }
}
