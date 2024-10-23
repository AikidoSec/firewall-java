package dev.aikido.agent_api.vulnerabilities.sql_injection;

import dev.aikido.agent_api.vulnerabilities.AikidoException;

public class SQLInjectionException extends AikidoException {
    public SQLInjectionException(String msg) {
        super(msg);
    }

    public static SQLInjectionException get(Dialect dialect) {
        String defaultMsg = generateDefaultMessage("SQL Injection");
        String msg = defaultMsg + ", Dialect: " + dialect.getHumanName();
        return new SQLInjectionException(msg);
    }
}
