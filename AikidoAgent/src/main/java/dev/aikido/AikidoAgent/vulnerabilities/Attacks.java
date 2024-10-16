package dev.aikido.AikidoAgent.vulnerabilities;

import dev.aikido.AikidoAgent.vulnerabilities.sql_injection.SqlDetector;

public class Attacks {
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
