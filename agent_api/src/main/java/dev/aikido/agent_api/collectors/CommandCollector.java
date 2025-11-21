package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import dev.aikido.agent_api.storage.statistics.OperationKind;
import dev.aikido.agent_api.storage.statistics.StatisticsStore;
import dev.aikido.agent_api.vulnerabilities.Scanner;
import dev.aikido.agent_api.vulnerabilities.Vulnerabilities;

import java.util.Arrays;
import java.util.List;

public final class CommandCollector {
    private CommandCollector() {}
    private static final Logger logger = LogManager.getLogger(CommandCollector.class);
    public static void report(String command) {
        if (command.isEmpty()) {
            return; // Empty command, don't scan.
        }

        logger.trace("Scanning command: %s", command);

        // report stats
        StatisticsStore.registerCall("runtime.Exec", OperationKind.EXEC_OP);

        // scan
        Vulnerabilities.Vulnerability vulnerability = new Vulnerabilities.ShellInjectionVulnerability();
        Scanner.scanForGivenVulnerability(vulnerability, "runtime.Exec", new String[]{command});
    }

    public static void report(List<String> commandArgs) {
        // This happens when Runtime.exec()'s are being called with multiple arguments -> gets forwarded.
        // or when new ProcessBuilder() is called. While we don't protect for argument injections, we do protect
        // against cases like ["sh", "-c", "<command>"]
        logger.trace("Scanning command arguments: %s", commandArgs);
        StatisticsStore.registerCall("ProcessBuilder.start", OperationKind.EXEC_OP);
        Vulnerabilities.Vulnerability vulnerability = new Vulnerabilities.ShellInjectionVulnerability();
        //Scanner.scanForGivenVulnerability(vulnerability, "runtime.Exec", commandArgs);
    }
}
