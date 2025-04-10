package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.storage.statistics.OperationKind;
import dev.aikido.agent_api.storage.statistics.StatisticsStore;
import dev.aikido.agent_api.vulnerabilities.Vulnerabilities;
import dev.aikido.agent_api.vulnerabilities.Scanner;

public final class SQLCollector {
    private SQLCollector() {}
    public static void report(String sql, String dialect, String operation) {
        // register statistics
        StatisticsStore.registerCall(operation, OperationKind.SQL_OP);

        // scan
        Vulnerabilities.Vulnerability vulnerability = new Vulnerabilities.SQLInjectionVulnerability();
        Scanner.scanForGivenVulnerability(vulnerability, operation, new String[]{sql, dialect});
    }
}
