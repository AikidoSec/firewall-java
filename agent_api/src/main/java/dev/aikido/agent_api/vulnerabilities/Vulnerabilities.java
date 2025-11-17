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

    private static final class SQLInjectionVulnerability implements Vulnerability {
        @Override
        public Detector getDetector() {
            return new SqlDetector();
        }
        @Override
        public String getKind() {
            return "sql_injection";
        }
    }
    public static final Vulnerability SQL_INJECTION = new SQLInjectionVulnerability();

    private static final class PathTraversalVulnerability implements Vulnerability {
        @Override
        public Detector getDetector() {
            return new PathTraversalDetector();
        }
        @Override
        public String getKind() {
            return "path_traversal";
        }
    }
    public static final Vulnerability PATH_TRAVERSAL = new PathTraversalVulnerability();

    private static final class SSRFVulnerability implements Vulnerability {
        @Override
        public Detector getDetector() { return null; }
        @Override
        public String getKind() {
            return "ssrf";
        }
    }
    public static final Vulnerability SSRF = new SSRFVulnerability();

    private static final class StoredSSRFVulnerability implements Vulnerability {
        @Override
        public Detector getDetector() { return null; }
        @Override
        public String getKind() {
            return "stored_ssrf";
        }
    }
    public static final Vulnerability STORED_SSRF = new StoredSSRFVulnerability();

    private static final class ShellInjectionVulnerability implements Vulnerability {
        @Override
        public Detector getDetector() { return new ShellInjectionDetector(); }
        @Override
        public String getKind() {
            return "shell_injection";
        }
    }
    public static final Vulnerability SHELL_INJECTION = new ShellInjectionVulnerability();
}
