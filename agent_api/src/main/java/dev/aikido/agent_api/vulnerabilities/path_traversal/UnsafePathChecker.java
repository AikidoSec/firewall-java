package dev.aikido.agent_api.vulnerabilities.path_traversal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class UnsafePathChecker {
    private UnsafePathChecker() {}
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
        String filePath = ensureOneLeadingSlash(filePathRaw.toLowerCase());

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
        String filePath = ensureOneLeadingSlash(filePathRaw.toLowerCase());
        String userInput = ensureOneLeadingSlash(userInputRaw.toLowerCase());
        return startsWithUnsafePath(filePath) && filePath.startsWith(userInput);
    }

    private static String ensureOneLeadingSlash(String path) {
        if (path.startsWith("/")) {
            return "/" + path.replaceAll("^/+", "");
        }
        return path;
    }
}
