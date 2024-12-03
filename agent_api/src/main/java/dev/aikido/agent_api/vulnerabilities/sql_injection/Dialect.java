package dev.aikido.agent_api.vulnerabilities.sql_injection;

import java.util.Objects;

public class Dialect {
    private final int rustDialectInt;
    private final String humanName;
    public Dialect(String dialect) {
        if (Objects.equals(dialect, "postgresql")) {
            rustDialectInt = 9;
            humanName = "PostgreSQL";
        } else if (Objects.equals(dialect, "mysql")) {
            rustDialectInt = 8;
            humanName = "MySQL";
        } else {
            rustDialectInt = 0; // Default option
            humanName = "Generic";
        }
    }

    public int getDialectInteger() {
        return rustDialectInt;
    }
    public String getHumanName() {
        return humanName;
    }
}
