package dev.aikido.agent_api.vulnerabilities.attack_wave_detection;

import dev.aikido.agent_api.helpers.ArrayHelpers;

import java.util.Set;

import static dev.aikido.agent_api.helpers.ArrayHelpers.containsIgnoreCase;
import static dev.aikido.agent_api.vulnerabilities.attack_wave_detection.Paths.*;

public final class PathChecker {
    private PathChecker() {
    }

    // Extensions that a Java app would not normally serve — only count as scan hits on 404
    private static final Set<String> FOREIGN_EXTENSIONS = Set.of(
        "php", "php3", "php4", "php5", "phtml"
    );

    public static boolean isWebScanPath(String path, int statusCode) {
        String normalized = path.toLowerCase();
        String[] segments = normalized.split("/");
        String filename = ArrayHelpers.lastElement(segments);

        if (filename != null) {
            if (containsIgnoreCase(FILE_NAMES, filename)) {
                return true;
            }
            // Check file extension
            int dotIndex = filename.lastIndexOf('.');
            if (dotIndex != -1) {
                String ext = filename.substring(dotIndex + 1);
                if (containsIgnoreCase(FILE_EXTENSIONS, ext)) {
                    return true;
                }
                // Foreign extensions (e.g. PHP) are only suspicious when the response is 404.
                // A 200 may mean the Java app is proxying to another backend.
                if (FOREIGN_EXTENSIONS.contains(ext) && statusCode == 404) {
                    return true;
                }
            }
        }

        // Check all directory names
        for (int i = 0; i < segments.length - 1; i++) {
            if (containsIgnoreCase(DIRECTORY_NAMES, segments[i])) {
                return true;
            }
        }

        return false;
    }
}
