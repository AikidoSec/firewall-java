package dev.aikido.AikidoAgent.collectors;

import dev.aikido.AikidoAgent.vulnerabilities.Attacks;
import dev.aikido.AikidoAgent.vulnerabilities.Scanner;

public class SQLCollector {
    public static void report(String sql, String dialect, String operation) {
        Attacks.Attack attack = new Attacks.SQLInjection();
        Scanner.scanForGivenVulnerability(attack, operation, new String[]{sql, dialect});
    }
}
