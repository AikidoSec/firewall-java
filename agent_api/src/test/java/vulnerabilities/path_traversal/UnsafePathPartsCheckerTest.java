package vulnerabilities.path_traversal;

import dev.aikido.agent_api.vulnerabilities.path_traversal.UnsafePathPartsChecker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UnsafePathPartsCheckerTest {
    @Test
    public void testSafePaths() {
        assertFalse(UnsafePathPartsChecker.containsUnsafePathParts("/home/user/file.txt"));
        assertFalse(UnsafePathPartsChecker.containsUnsafePathParts("C:\\Users\\User\\Documents\\file.txt"));
        assertFalse(UnsafePathPartsChecker.containsUnsafePathParts("C:/Program Files/app.exe"));
    }

    @Test
    public void testDangerousPaths() {
        assertTrue(UnsafePathPartsChecker.containsUnsafePathParts("/home/user/../file.txt"));
        assertTrue(UnsafePathPartsChecker.containsUnsafePathParts("C:\\Users\\User\\..\\Documents\\file.txt"));
        assertTrue(UnsafePathPartsChecker.containsUnsafePathParts("..\\..\\file.txt"));
        assertTrue(UnsafePathPartsChecker.containsUnsafePathParts("../folder/file.txt"));
    }

    @Test
    public void testEdgeCases() {
        assertFalse(UnsafePathPartsChecker.containsUnsafePathParts(""));
        assertFalse(UnsafePathPartsChecker.containsUnsafePathParts(".."));
        assertFalse(UnsafePathPartsChecker.containsUnsafePathParts("."));
        assertTrue(UnsafePathPartsChecker.containsUnsafePathParts("folder/../file.txt"));
        assertTrue(UnsafePathPartsChecker.containsUnsafePathParts("folder\\..\\file.txt"));
    }
}
