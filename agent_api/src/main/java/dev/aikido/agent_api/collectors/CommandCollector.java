package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import dev.aikido.agent_api.storage.statistics.OperationKind;
import dev.aikido.agent_api.storage.statistics.StatisticsStore;
import dev.aikido.agent_api.vulnerabilities.Scanner;
import dev.aikido.agent_api.vulnerabilities.Vulnerabilities;

public final class CommandCollector {
    private CommandCollector() {}
    private static final Logger logger = LogManager.getLogger(CommandCollector.class);
    public static void report(Object command) {
        if (command instanceof String commandStr) {
            if (commandStr.isEmpty()) {
                return; // Empty command, don't scan.
            }

            logger.trace("Scanning command: %s", commandStr);

            // report stats
            StatisticsStore.registerCall("runtime.Exec", OperationKind.EXEC_OP);

            // scan
            Scanner.scanForGivenVulnerability(Vulnerabilities.SHELL_INJECTION, "runtime.Exec", new String[]{commandStr});
        }
    }
}
