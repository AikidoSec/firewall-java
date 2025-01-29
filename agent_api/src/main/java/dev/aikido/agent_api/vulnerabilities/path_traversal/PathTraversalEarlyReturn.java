package dev.aikido.agent_api.vulnerabilities.path_traversal;

import static dev.aikido.agent_api.vulnerabilities.path_traversal.UnsafePathChecker.startsWithUnsafePath;
import static dev.aikido.agent_api.vulnerabilities.path_traversal.UnsafePathPartsChecker.containsUnsafePathParts;

/**
 * To increase performance, we allow vulnerability algorithms to implement an early return
 * this means that we do not loop over all user input in the request given certain parameters
 * For Path Traversal: If the file path we are scanning does not contain unsafe path parts or starts unsafe,
 * we can do an early return (i.e. not loop over user input)
 */
public final class PathTraversalEarlyReturn {
    private PathTraversalEarlyReturn() {
    }

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
