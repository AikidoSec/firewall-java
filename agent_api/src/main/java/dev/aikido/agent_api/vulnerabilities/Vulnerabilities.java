package dev.aikido.agent_api.vulnerabilities;

import dev.aikido.agent_api.vulnerabilities.path_traversal.PathTraversalDetector;
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
    public static final class PathTraversalVulnerability implements Vulnerability {
        @Override
        public Detector getDetector() {
            return new PathTraversalDetector();
        }
        @Override
        public String getKind() {
            return "path_traversal";
        }
    }
}
