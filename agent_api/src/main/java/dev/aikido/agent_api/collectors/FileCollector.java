package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.vulnerabilities.Scanner;
import dev.aikido.agent_api.vulnerabilities.Vulnerabilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.nio.file.Path;

public final class FileCollector {
    private FileCollector() {}
    public static void report(Object filePath, String operation) {
        if (filePath == null) {
            return; // Make sure filePath is defined
        }
        Vulnerabilities.Vulnerability vulnerability = new Vulnerabilities.PathTraversalVulnerability();
        if (filePath instanceof String filePathString) {
            Scanner.scanForGivenVulnerability(vulnerability, operation, new String[]{filePathString});
        } else if (filePath instanceof URI filePathURI) {
            // File(...) Constructor also accepts URI objects, but path remains the same
            // So we just extract the path here : (i.e. new File("file:///../test.txt") --> "/../test.txt")
            String filePathString = filePathURI.getPath();
            Scanner.scanForGivenVulnerability(vulnerability, operation, new String[]{filePathString});
        } else if (filePath instanceof Path filePathAsPath) {
            // Some functions on Path also accept other paths
            String filePathString = filePathAsPath.toString();
            Scanner.scanForGivenVulnerability(vulnerability, operation, new String[]{filePathString});
        } else if (filePath instanceof Object[] filePaths) {
            // In Paths.get() sometimes you can have multiple paths provided, scan them individually :
            for (int i = 0; i < filePaths.length; i++) {
                report(filePaths[i], operation);
            }
        }
    }
}
