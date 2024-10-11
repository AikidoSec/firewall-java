package dev.aikido.AikidoAgent.collectors;

public class SQLCollector {
    public static void report(String sql, String dialect) {
        System.out.println("SQL : " + sql + ", Dialect : " + dialect);
    }
}
