package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.vulnerabilities.Scanner;
import dev.aikido.agent_api.vulnerabilities.Vulnerabilities;

public final class FileCollector {
    private FileCollector() {}
    public static void report(Object filePath) {
        if (filePath == null) {
            return; // Make sure filePath is defined
        }
        if (filePath instanceof String filePathString) {
            Vulnerabilities.Vulnerability vulnerability = new Vulnerabilities.PathTraversalVulnerability();
            Scanner.scanForGivenVulnerability(vulnerability, "java.io.File", new String[]{filePathString});
        }
    }
}
