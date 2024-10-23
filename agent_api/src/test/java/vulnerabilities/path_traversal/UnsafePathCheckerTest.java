package vulnerabilities.path_traversal;

import dev.aikido.agent_api.vulnerabilities.path_traversal.UnsafePathChecker;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UnsafePathCheckerTest {

    @Test
    public void testLinuxRootPaths() {
        assertTrue(UnsafePathChecker.startsWithUnsafePath("/etc/passwd", "/etc"));
        assertTrue(UnsafePathChecker.startsWithUnsafePath("/bin/bash", "/bin"));
        assertTrue(UnsafePathChecker.startsWithUnsafePath("/lib/modules", "/lib"));
        assertTrue(UnsafePathChecker.startsWithUnsafePath("/home/user/file.txt", "/home"));
        assertTrue(UnsafePathChecker.startsWithUnsafePath("/usr/local/bin", "/usr"));
        assertTrue(UnsafePathChecker.startsWithUnsafePath("/var/log/syslog", "/var"));
    }

    @Test
    public void testWindowsPaths() {
        assertTrue(UnsafePathChecker.startsWithUnsafePath("c:/Program Files/app.exe", "c:/"));
        assertTrue(UnsafePathChecker.startsWithUnsafePath("c:\\Windows\\System32\\cmd.exe", "c:\\"));
        assertFalse(UnsafePathChecker.startsWithUnsafePath("d:/Documents/file.txt", "c:/"));
    }

    @Test
    public void testEdgeCases() {
        assertFalse(UnsafePathChecker.startsWithUnsafePath("", "/etc"));
        assertFalse(UnsafePathChecker.startsWithUnsafePath("/etc", ""));
        assertTrue(UnsafePathChecker.startsWithUnsafePath("c:/", "c:/"));
        assertTrue(UnsafePathChecker.startsWithUnsafePath("c:/folder/file.txt", "c:/folder"));
    }
}
