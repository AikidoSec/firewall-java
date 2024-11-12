package dev.aikido.agent_api.vulnerabilities;

import dev.aikido.agent_api.vulnerabilities.sql_injection.SqlDetector;

public final class Attacks {
    private Attacks() {}
    public interface Attack {
        Detector getDetector();
    }
    public static final class SQLInjection implements Attack {
        @Override
        public Detector getDetector() {
            return new SqlDetector();
        }
    }
}
