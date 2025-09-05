package dev.aikido.agent_api.vulnerabilities.attack_wave_detection;

import dev.aikido.agent_api.helpers.ArrayHelpers;

import static dev.aikido.agent_api.helpers.ArrayHelpers.containsIgnoreCase;
import static dev.aikido.agent_api.vulnerabilities.attack_wave_detection.Paths.*;

public final class PathChecker {
    private PathChecker() {
    }

    public static boolean isWebScanPath(String path) {
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
