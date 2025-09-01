package dev.aikido.agent_api.vulnerabilities.attack_wave_detection;

import dev.aikido.agent_api.helpers.ArrayHelpers;

import static dev.aikido.agent_api.vulnerabilities.attack_wave_detection.Paths.*;

public final class PathChecker {
    private PathChecker() {
    }

    public static boolean isWebScanPath(String path) {
        String normalized = path.toLowerCase();
        String[] segments = normalized.split("/");
        String filename = ArrayHelpers.pop(segments);

        if (filename != null) {
            if (FILE_NAMES.contains(filename)) {
                return true;
            }
            // Check file extension
            int dotIndex = filename.lastIndexOf('.');
            if (dotIndex != -1) {
                String ext = filename.substring(dotIndex + 1);
                if (FILE_EXTENSIONS.contains(ext)) {
                    return true;
                }
            }
        }

        // Check all directory names
        for (int i = 0; i < segments.length - 1; i++) {
            if (DIRECTORY_NAMES.contains(segments[i])) {
                return true;
            }
        }

        return false;
    }
}
