package dev.aikido.AikidoAgent.collectors;

import dev.aikido.AikidoAgent.vulnerabilities.Attacks;
import dev.aikido.AikidoAgent.vulnerabilities.Scanner;

public class SQLCollector {
    public static void report(String sql, String dialect, String operation) {
        System.out.println("SQL : " + sql + ", Dialect : " + dialect);
        Attacks.Attack attack = new Attacks.SQLInjection();
        Scanner.run(attack, operation, new String[]{sql, dialect});
    }
}
