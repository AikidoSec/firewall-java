package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.vulnerabilities.Scanner;
import dev.aikido.agent_api.vulnerabilities.Vulnerabilities;

import java.net.URI;

public final class FileCollector {
    private FileCollector() {}
    public static void report(Object filePath) {
        if (filePath == null) {
            return; // Make sure filePath is defined
        }
        Vulnerabilities.Vulnerability vulnerability = new Vulnerabilities.PathTraversalVulnerability();
        if (filePath instanceof String filePathString) {
            Scanner.scanForGivenVulnerability(vulnerability, "java.io.File", new String[]{filePathString});
        } else if (filePath instanceof URI filePathURI) {
            // File(...) Constructor also accepts URI objects, but path remains the same
            // So we just extract the path here : (i.e. new File("file:///../test.txt") --> "/../test.txt")
            String filePathString = filePathURI.getPath();
            Scanner.scanForGivenVulnerability(vulnerability, "java.io.File", new String[]{filePathString});
        }
    }
}
