package dev.aikido.agent;

import dev.aikido.agent_api.background.BackgroundProcess;
import dev.aikido.agent_api.helpers.env.Token;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import static dev.aikido.agent.helpers.AgentArgumentParser.parseAgentArgs;

public final class DaemonStarter {
    private DaemonStarter() {}
    private static final Logger logger = LogManager.getLogger(DaemonStarter.class);

    public static void startDaemon(String agentArgs) {
        DAEMON_MODE daemonMode = getDaemonMode(agentArgs);
        if (daemonMode == DAEMON_MODE.DAEMON_ENABLED) {
            // Start the background process only if the daemon is enabled.
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

    private enum DAEMON_MODE {
        DAEMON_DISABLED,
        DAEMON_ENABLED
    }
    private static DAEMON_MODE getDaemonMode(String agentArgs) {
        if (parseAgentArgs(agentArgs).containsKey("mode")) {
            String mode = parseAgentArgs(agentArgs).get("mode");
            if (mode.equals("daemon-disabled")) {
                // Background process is disabled, return :
                logger.info("Running with background process disabled (mode: daemon-disabled)");
                return DAEMON_MODE.DAEMON_DISABLED;
            }
        }
        return DAEMON_MODE.DAEMON_ENABLED;
    }
}
