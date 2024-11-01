package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.vulnerabilities.Vulnerabilities;
import dev.aikido.agent_api.vulnerabilities.Scanner;

public class SQLCollector {
    public static void report(String sql, String dialect, String operation) {
        Vulnerabilities.Vulnerability vulnerability = new Vulnerabilities.SQLInjectionVulnerability();
        Scanner.scanForGivenVulnerability(vulnerability, operation, new String[]{sql, dialect});
    }
}
