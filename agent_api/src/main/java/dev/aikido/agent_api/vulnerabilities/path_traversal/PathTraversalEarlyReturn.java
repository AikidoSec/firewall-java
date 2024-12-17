package dev.aikido.agent_api.vulnerabilities.path_traversal;

import static dev.aikido.agent_api.vulnerabilities.path_traversal.UnsafePathChecker.startsWithUnsafePath;
import static dev.aikido.agent_api.vulnerabilities.path_traversal.UnsafePathPartsChecker.containsUnsafePathParts;

public final class PathTraversalEarlyReturn {
    private PathTraversalEarlyReturn() {}
    public static boolean shouldReturnEarly(String filePath) {
        if (containsUnsafePathParts(filePath)) {
            return false; // Should not return early: path contains dangerous parts, so inspect user input
        }
        if (startsWithUnsafePath(filePath)) {
            return false; // Should not return early: path starts unsafe, so inspect user input
        }
        return true; // If no unsafe parts and start is fine, return early.
    }
}
