package dev.aikido.agent_api.vulnerabilities.path_traversal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public final class UnsafePathChecker {
    private UnsafePathChecker() {}

    private static final Pattern CURRENT_DIR_PATTERN = Pattern.compile("/(\\./)+");

    private static final List<String> LINUX_ROOT_FOLDERS = Arrays.asList(
            "/bin/",
            "/boot/",
            "/dev/",
            "/etc/",
            "/home/",
            "/init/",
            "/lib/",
            "/media/",
            "/mnt/",
            "/opt/",
            "/proc/",
            "/root/",
            "/run/",
            "/sbin/",
            "/srv/",
            "/sys/",
            "/tmp/",
            "/usr/",
            "/var/"
    );
    private static final List<String> DANGEROUS_PATH_STARTS = Arrays.asList(
            "c:/",
            "c:\\"
    );

    public static boolean startsWithUnsafePath(String filePathRaw) {
        String filePath = normalizePath(filePathRaw.toLowerCase());

        List<String> dangerousStartsList = new ArrayList<>(DANGEROUS_PATH_STARTS);
        dangerousStartsList.addAll(LINUX_ROOT_FOLDERS);

        for (String dangerousStart : dangerousStartsList) {
            if (filePath.startsWith(dangerousStart)) {
                return true;
            }
        }
        return false;
    }

    public static boolean startsWithUnsafePath(String filePathRaw, String userInputRaw) {
        String filePath = normalizePath(filePathRaw.toLowerCase());
        String userInput = normalizePath(userInputRaw.toLowerCase());
        return startsWithUnsafePath(filePath) && filePath.startsWith(userInput);
    }

    /**
     * Normalizes a path by removing /./ and removing consecutive slashes
     */
    private static String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }

        // Matches /./ or /././ or /./././ etc. (one or more ./ sequences after a /)
        String normalized = CURRENT_DIR_PATTERN.matcher(path).replaceAll("/");

        // Merge consecutive slashes since these don't change where you are in the path.
        normalized = normalized.replaceAll("/+", "/");
        return normalized;
    }
}
