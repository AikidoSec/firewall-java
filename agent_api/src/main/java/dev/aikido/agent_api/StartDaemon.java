package dev.aikido.agent_api;

import dev.aikido.agent_api.background.BackgroundProcess;
import dev.aikido.agent_api.helpers.env.Token;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

public final class StartDaemon {
    private static final Logger logger = LogManager.getLogger(StartDaemon.class);

    public static void startDaemon() {
        Token token = Token.fromEnv();
        if (token == null) {
            logger.info("Failed to start background process due to an invalid token");
            return;
        }
        startDaemon(token);
    }

    public static void startDaemon(Token token) {
        BackgroundProcess backgroundProcess = new BackgroundProcess("main-background-process", token);
        backgroundProcess.setDaemon(true);
        backgroundProcess.start();
    }
}
