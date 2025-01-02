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

    public static boolean startsWithUnsafePath(String filePath) {
        String lowerCasePath = filePath.toLowerCase();

        List<String> dangerousStartsList = new ArrayList<>(DANGEROUS_PATH_STARTS);
        dangerousStartsList.addAll(LINUX_ROOT_FOLDERS);

        for (String dangerousStart : dangerousStartsList) {
            if (lowerCasePath.startsWith(dangerousStart)) {
                return true;
            }
        }
        return false;
    }
    public static boolean startsWithUnsafePath(String filePath, String userInput) {
        String filePathLowercase = filePath.toLowerCase();
        String userinputLowercase = userInput.toLowerCase();
        return startsWithUnsafePath(filePathLowercase) && filePathLowercase.startsWith(userinputLowercase);
    }
}
