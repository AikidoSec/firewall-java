package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.storage.statistics.OperationKind;
import dev.aikido.agent_api.storage.statistics.StatisticsStore;
import dev.aikido.agent_api.vulnerabilities.Scanner;
import dev.aikido.agent_api.vulnerabilities.Vulnerabilities;

public final class CommandCollector {
    private CommandCollector() {}
    public static void report(Object command) {
        if (command instanceof String commandStr) {
            if (commandStr.isEmpty()) {
                return; // Empty command, don't scan.
            }

            // report stats
            StatisticsStore.registerCall("runtime.Exec", OperationKind.EXEC_OP);

            // scan
            Vulnerabilities.Vulnerability vulnerability = new Vulnerabilities.ShellInjectionVulnerability();
            Scanner.scanForGivenVulnerability(vulnerability, "runtime.Exec", new String[]{commandStr});
        }
    }
}
