package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import dev.aikido.agent_api.storage.statistics.OperationKind;
import dev.aikido.agent_api.storage.statistics.StatisticsStore;
import dev.aikido.agent_api.vulnerabilities.Vulnerabilities;
import dev.aikido.agent_api.vulnerabilities.Scanner;

public final class SQLCollector {
    private SQLCollector() {}
    private static final Logger logger = LogManager.getLogger(SQLCollector.class);
    public static void report(String sql, String dialect, String operation) {
        // register statistics
        StatisticsStore.registerCall(operation, OperationKind.SQL_OP);

        // scan
        Scanner.scanForGivenVulnerability(Vulnerabilities.SQL_INJECTION, operation, new String[]{sql, dialect});
    }
}
