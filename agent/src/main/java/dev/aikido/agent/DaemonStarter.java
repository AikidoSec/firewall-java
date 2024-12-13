package dev.aikido.agent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;

import static dev.aikido.agent.helpers.AgentArgumentParser.parseAgentArgs;
import static dev.aikido.agent.helpers.ClassLoader.fetchMethod;

public final class DaemonStarter {
    private DaemonStarter() {}
    private static final Logger logger = LogManager.getLogger(DaemonStarter.class);

    public static void startDaemon(String agentArgs) {
        DAEMON_MODE daemonMode = getDaemonMode(agentArgs);
        if (daemonMode == DAEMON_MODE.DAEMON_ENABLED) {
            // Start the background process only if the daemon is enabled.
            Method startMethod = fetchMethod("dev.aikido.agent_api.background.Starter", "start");
            try {
                startMethod.invoke(null);
            } catch (Exception e) {
                logger.info(e);
            }
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
