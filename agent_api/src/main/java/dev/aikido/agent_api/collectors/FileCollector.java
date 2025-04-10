package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.storage.statistics.OperationKind;
import dev.aikido.agent_api.storage.statistics.StatisticsStore;
import dev.aikido.agent_api.vulnerabilities.Scanner;
import dev.aikido.agent_api.vulnerabilities.Vulnerabilities;

import java.net.URI;
import java.nio.file.Path;

public final class FileCollector {
    private FileCollector() {}
    private static final int MAX_RECURSION_DEPTH = 3;

    public static void report(Object filePath, String operation) {
        report(filePath, operation, 0); // Start with depth of zero
    }
    public static void report(Object filePath, String operation, int depth) {
        if (filePath == null) {
            return; // Make sure filePath is defined
        }

        StatisticsStore.registerCall(operation, OperationKind.FS_OP);

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
            if (depth >= MAX_RECURSION_DEPTH) {
                return;
            }
            for (int i = 0; i < filePaths.length; i++) {
                report(filePaths[i], operation, depth + 1);
            }
        }
    }
}
