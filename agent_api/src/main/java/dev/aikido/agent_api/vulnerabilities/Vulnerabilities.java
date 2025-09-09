package dev.aikido.agent_api.vulnerabilities;

import dev.aikido.agent_api.vulnerabilities.path_traversal.PathTraversalDetector;
import dev.aikido.agent_api.vulnerabilities.shell_injection.ShellInjectionDetector;
import dev.aikido.agent_api.vulnerabilities.sql_injection.SqlDetector;
import dev.aikido.agent_api.vulnerabilities.ssrf.SSRFDetector;

public final class Vulnerabilities {
    private Vulnerabilities() {}
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
    public static final class SSRFVulnerability implements Vulnerability {
        @Override
        public Detector getDetector() { return null; }
        @Override
        public String getKind() {
            return "ssrf";
        }
    }
    public static final class StoredSSRFVulnerability implements Vulnerability {
        @Override
        public Detector getDetector() { return null; }
        @Override
        public String getKind() {
            return "stored-ssrf";
        }
    }
    public static final class ShellInjectionVulnerability implements Vulnerability {
        @Override
        public Detector getDetector() { return new ShellInjectionDetector(); }
        @Override
        public String getKind() {
            return "shell_injection";
        }
    }
}
