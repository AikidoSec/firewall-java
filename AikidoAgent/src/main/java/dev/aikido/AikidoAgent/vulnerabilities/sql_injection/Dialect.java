package dev.aikido.AikidoAgent.vulnerabilities.sql_injection;

import java.util.Objects;

public class Dialect {
    private final int rustDialectInt;
    public Dialect(String dialect) {
        if (Objects.equals(dialect, "postgres")) {
            rustDialectInt = 9;
        } else if (Objects.equals(dialect, "mysql")) {
            rustDialectInt = 8;
        } else {
            rustDialectInt = 0; // Default option
        }
    }

    public int getDialectInteger() {
        return rustDialectInt;
    }
}
