package dev.aikido.agent_api.vulnerabilities;

import dev.aikido.agent_api.vulnerabilities.sql_injection.SqlDetector;

public class Vulnerabilities {
    public interface Vulnerability {
        Detector getDetector();
        String getKind();
    }
    public static final class SQLInjectionVulnerability implements Vulnerability {
        @Override
        public Detector getDetector() {
            return new SqlDetector();
        }
        @Override
        public String getKind() {
            return "sql_injection";
        }
    }
}
