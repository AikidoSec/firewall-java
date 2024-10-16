package dev.aikido.AikidoAgent.collectors;

import dev.aikido.AikidoAgent.vulnerabilities.Vulnerabilities;
import dev.aikido.AikidoAgent.vulnerabilities.Scanner;

public class SQLCollector {
    public static void report(String sql, String dialect, String operation) {
        Vulnerabilities.Vulnerability vulnerability = new Vulnerabilities.SQLInjectionVulnerability();
        Scanner.scanForGivenVulnerability(vulnerability, operation, new String[]{sql, dialect});
    }
}
