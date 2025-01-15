package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import dev.aikido.agent_api.vulnerabilities.Scanner;
import dev.aikido.agent_api.vulnerabilities.Vulnerabilities;

public final class CommandCollector {
    private CommandCollector() {}
    private static final Logger logger = LogManager.getLogger(CommandCollector.class);

    public static void report(Object command) {
        logger.info("With : %s", command);
        if (command instanceof String commandStr) {
            if (commandStr.isEmpty()) {
                return; // Empty command, don't scan.
            }
            Vulnerabilities.Vulnerability vulnerability = new Vulnerabilities.ShellInjectionVulnerability();
            Scanner.scanForGivenVulnerability(vulnerability, "runtime.Exec", new String[]{commandStr});
        }
    }
}
