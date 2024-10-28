package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.vulnerabilities.Scanner;
import dev.aikido.agent_api.vulnerabilities.Vulnerabilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileCollector {
    private static final Logger logger = LogManager.getLogger(FileCollector.class);
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
