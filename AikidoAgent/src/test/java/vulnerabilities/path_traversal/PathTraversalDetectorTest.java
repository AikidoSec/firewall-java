package vulnerabilities.path_traversal;

import dev.aikido.AikidoAgent.vulnerabilities.path_traversal.PathTraversalDetector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PathTraversalDetectorTest {
    private final PathTraversalDetector pathTraversalDetector = new PathTraversalDetector();
    @Test
    public void testEmptyUserInput() {
        assertFalse(pathTraversalDetector.run("", new String[]{"test.txt"}));
    }

    @Test
    public void testEmptyFileInput() {
        assertFalse(pathTraversalDetector.run("test", new String[]{""}));
    }

    @Test
    public void testEmptyUserInputAndFileInput() {
        assertFalse(pathTraversalDetector.run("", new String[]{""}));
    }

    @Test
    public void testUserInputIsASingleCharacter() {
        assertFalse(pathTraversalDetector.run("t", new String[]{"test.txt"}));
    }

    @Test
    public void testFileInputIsASingleCharacter() {
        assertFalse(pathTraversalDetector.run("test", new String[]{"t"}));
    }

    @Test
    public void testSameAsUserInput() {
        assertFalse(pathTraversalDetector.run("text.txt", new String[]{"text.txt"}));
    }

    @Test
    public void testWithDirectoryBefore() {
        assertFalse(pathTraversalDetector.run("text.txt", new String[]{"directory/text.txt"}));
    }

    @Test
    public void testWithBothDirectoryBefore() {
        assertFalse(pathTraversalDetector.run("directory/text.txt", new String[]{"directory/text.txt"}));
    }

    @Test
    public void testUserInputAndFileInputAreSingleCharacters() {
        assertFalse(pathTraversalDetector.run("t", new String[]{"t"}));
    }

    @Test
    public void testItFlagsDotDotSlash() {
        assertTrue(pathTraversalDetector.run("../", new String[]{"../test.txt"}));
    }

    @Test
    public void testItFlagsBackslashDotDot() {
        assertTrue(pathTraversalDetector.run("..\\", new String[]{"..\\test.txt",}));
    }

    @Test
    public void testItFlagsDoubleDotSlash() {
        assertTrue(pathTraversalDetector.run("../../", new String[]{"../../test.txt"}));
    }

    @Test
    public void testItFlagsDoubleDotBackslash() {
        assertTrue(pathTraversalDetector.run("..\\..\\", new String[]{"..\\..\\test.txt",}));
    }

    @Test
    public void testItFlagsFourDotSlash() {
        assertTrue(pathTraversalDetector.run("../../../../", new String[]{"../../../../test.txt"}));
    }

    @Test
    public void testItFlagsThreeDotBackslash() {
        assertTrue(pathTraversalDetector.run("..\\..\\..\\", new String[]{"..\\..\\..\\test.txt",}));
    }

    @Test
    public void testUserInputIsLongerThanFilePath() {
        assertFalse(pathTraversalDetector.run("../../file.txt", new String[]{"../file.txt"}));
    }

    @Test
    public void testAbsoluteLinuxPath() {
        assertTrue(pathTraversalDetector.run("/etc/passwd", new String[]{"/etc/passwd"}));
    }

    @Test
    public void testLinuxUserDirectory() {
        assertTrue(pathTraversalDetector.run("/home/user/", new String[]{"/home/user/file.txt"}));
    }

    @Test
    public void testWindowsDriveLetter() {
        assertTrue(pathTraversalDetector.run("C:\\", new String[]{"C:\\file.txt"}));
    }

    @Test
    public void testNoPathTraversal() {
        assertFalse(pathTraversalDetector.run("/storage/file.txt", new String[]{"/appdata/storage/file.txt"}));
    }

    @Test
    public void testDoesNotFlagTest() {
        assertFalse(pathTraversalDetector.run("test", new String[]{"/app/test.txt"}));
    }

    @Test
    public void testDoesNotFlagExampleTestTxt() {
        assertFalse(pathTraversalDetector.run("example/test.txt", new String[]{"/app/data/example/test.txt"}));
    }

    @Test
    public void testDoesNotAbsolutePathWithDifferentFolder() {
        assertFalse(pathTraversalDetector.run("/etc/hack/config", new String[]{"/etc/app/config"}));
    }

    @Test
    public void testDoesNotAbsolutePathInsideAnotherFolder() {
        assertFalse(pathTraversalDetector.run("/etc/config", new String[]{"/etc/app/data/etc/config"}));
    }
}