package dev.aikido.agent_api.vulnerabilities.path_traversal;

import java.util.Arrays;
import java.util.List;

public final class UnsafePathPartsChecker {
    private UnsafePathPartsChecker() {
    }

    private static final List<String> DANGEROUS_PATH_PARTS = Arrays.asList("../", "..\\");

    public static boolean containsUnsafePathParts(String filePath) {
        for (String dangerousPart : DANGEROUS_PATH_PARTS) {
            if (filePath.contains(dangerousPart)) {
                return true;
            }
        }
        return false;
    }
}
