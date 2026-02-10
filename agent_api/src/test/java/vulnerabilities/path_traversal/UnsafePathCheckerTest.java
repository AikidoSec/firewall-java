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

        // Docker container common directories :
        assertTrue(UnsafePathChecker.startsWithUnsafePath("/app/config.yml", "/app"));
        assertTrue(UnsafePathChecker.startsWithUnsafePath("/code/src/main.py", "/code"));
        assertTrue(UnsafePathChecker.startsWithUnsafePath("/app/config.yml"));
        assertTrue(UnsafePathChecker.startsWithUnsafePath("/code/src/main.py"));

        // Capitalization checks :
        assertTrue(UnsafePathChecker.startsWithUnsafePath("/var/log/syslog", "/VaR"));
        assertTrue(UnsafePathChecker.startsWithUnsafePath("/home/user/file.txt", "/HoMe"));
        assertTrue(UnsafePathChecker.startsWithUnsafePath("/HOME/user/file.txt", "/home"));
        assertTrue(UnsafePathChecker.startsWithUnsafePath("/bIn/bash", "/bin"));

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

    @Test
    public void testMultipleSlashes() {
        assertTrue(UnsafePathChecker.startsWithUnsafePath("///etc///passwd", "///etc//"));
        assertTrue(UnsafePathChecker.startsWithUnsafePath("///etc/passwd", "///etc"));
        assertFalse(UnsafePathChecker.startsWithUnsafePath("etc/passwd///../test.txt", "etc/passwd///../test.txt"));

        assertTrue(UnsafePathChecker.startsWithUnsafePath("///etc///passwd"));
        assertTrue(UnsafePathChecker.startsWithUnsafePath("///etc/passwd"));
        assertFalse(UnsafePathChecker.startsWithUnsafePath("etc/passwd///../test.txt"));
    }

    @Test
    public void testCurrentDirectoryReferences() {
        // /./ should be normalized to /
        assertTrue(UnsafePathChecker.startsWithUnsafePath("/./etc/passwd", "/./etc"));
        assertTrue(UnsafePathChecker.startsWithUnsafePath("/etc/./passwd", "/etc"));
        assertTrue(UnsafePathChecker.startsWithUnsafePath("/etc/./passwd", "/etc/./"));

        assertTrue(UnsafePathChecker.startsWithUnsafePath("/./etc/passwd"));
        assertTrue(UnsafePathChecker.startsWithUnsafePath("/etc/./passwd"));
        assertTrue(UnsafePathChecker.startsWithUnsafePath("/./etc/./passwd"));

        // Multiple /./ sequences
        assertTrue(UnsafePathChecker.startsWithUnsafePath("/././etc/passwd", "/././etc"));
        assertTrue(UnsafePathChecker.startsWithUnsafePath("/etc/././passwd"));
    }

    @Test
    public void testPathNormalization() {
        // Paths with multiple slashes and /./ should be normalized and detected
        assertTrue(UnsafePathChecker.startsWithUnsafePath("//etc//passwd", "/etc"));
        assertTrue(UnsafePathChecker.startsWithUnsafePath("/./etc/./passwd", "/etc"));
        assertTrue(UnsafePathChecker.startsWithUnsafePath("/././etc/passwd", "/etc"));

        // Paths without leading slash are not unsafe
        assertFalse(UnsafePathChecker.startsWithUnsafePath("etc/passwd", "etc"));
        assertFalse(UnsafePathChecker.startsWithUnsafePath("", ""));

        // Combined slashes and dot: ///.///etc/passwd should normalize to /etc/passwd
        assertTrue(UnsafePathChecker.startsWithUnsafePath("///.///etc/passwd", "///.///etc"));
        assertTrue(UnsafePathChecker.startsWithUnsafePath("///.///etc/passwd"));
    }
}
