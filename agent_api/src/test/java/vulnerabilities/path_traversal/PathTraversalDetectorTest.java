package vulnerabilities.path_traversal;

import dev.aikido.agent_api.vulnerabilities.Detector;
import dev.aikido.agent_api.vulnerabilities.path_traversal.PathTraversalDetector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PathTraversalDetectorTest {
    private final PathTraversalDetector pathTraversalDetector = new PathTraversalDetector();
    private void assertNotAttack(Detector.DetectorResult detectorResult) {
        assertFalse(detectorResult.isDetectedAttack());
    }
    private void assertAttack(Detector.DetectorResult detectorResult) {
        assertTrue(detectorResult.isDetectedAttack());
    }
    @Test
    public void testEmptyUserInput() {
        assertNotAttack(pathTraversalDetector.run("", new String[]{"test.txt"}));
    }

    @Test
    public void testEmptyFileInput() {
        assertNotAttack(pathTraversalDetector.run("test", new String[]{""}));
    }

    @Test
    public void testEmptyUserInputAndFileInput() {
        assertNotAttack(pathTraversalDetector.run("", new String[]{""}));
    }

    @Test
    public void testUserInputIsASingleCharacter() {
        assertNotAttack(pathTraversalDetector.run("t", new String[]{"test.txt"}));
    }

    @Test
    public void testFileInputIsASingleCharacter() {
        assertNotAttack(pathTraversalDetector.run("test", new String[]{"t"}));
    }

    @Test
    public void testSameAsUserInput() {
        assertNotAttack(pathTraversalDetector.run("text.txt", new String[]{"text.txt"}));
    }

    @Test
    public void testWithDirectoryBefore() {
        assertNotAttack(pathTraversalDetector.run("text.txt", new String[]{"directory/text.txt"}));
    }

    @Test
    public void testWithBothDirectoryBefore() {
        assertNotAttack(pathTraversalDetector.run("directory/text.txt", new String[]{"directory/text.txt"}));
    }

    @Test
    public void testUserInputAndFileInputAreSingleCharacters() {
        assertNotAttack(pathTraversalDetector.run("t", new String[]{"t"}));
    }

    @Test
    public void testItFlagsDotDotSlash() {
        assertAttack(pathTraversalDetector.run("../", new String[]{"../test.txt"}));
    }

    @Test
    public void testItFlagsBackslashDotDot() {
        assertAttack(pathTraversalDetector.run("..\\", new String[]{"..\\test.txt",}));
    }

    @Test
    public void testItFlagsDoubleDotSlash() {
        assertAttack(pathTraversalDetector.run("../../", new String[]{"../../test.txt"}));
    }

    @Test
    public void testItFlagsDoubleDotBackslash() {
        assertAttack(pathTraversalDetector.run("..\\..\\", new String[]{"..\\..\\test.txt",}));
    }

    @Test
    public void testItFlagsFourDotSlash() {
        assertAttack(pathTraversalDetector.run("../../../../", new String[]{"../../../../test.txt"}));
    }

    @Test
    public void testItFlagsThreeDotBackslash() {
        assertAttack(pathTraversalDetector.run("..\\..\\..\\", new String[]{"..\\..\\..\\test.txt",}));
    }

    @Test
    public void testUserInputIsLongerThanFilePath() {
        assertNotAttack(pathTraversalDetector.run("../../file.txt", new String[]{"../file.txt"}));
    }

    @Test
    public void testAbsoluteLinuxPath() {
        assertAttack(pathTraversalDetector.run("/etc/passwd", new String[]{"/etc/passwd"}));
    }

    @Test
    public void testLinuxUserDirectory() {
        assertAttack(pathTraversalDetector.run("/home/user/", new String[]{"/home/user/file.txt"}));
    }

    @Test
    public void testWindowsDriveLetter() {
        assertAttack(pathTraversalDetector.run("C:\\", new String[]{"C:\\file.txt"}));
    }

    @Test
    public void testNoPathTraversal() {
        assertNotAttack(pathTraversalDetector.run("/storage/file.txt", new String[]{"/appdata/storage/file.txt"}));
    }

    @Test
    public void testDoesNotFlagTest() {
        assertNotAttack(pathTraversalDetector.run("test", new String[]{"/app/test.txt"}));
    }

    @Test
    public void testDoesNotFlagExampleTestTxt() {
        assertNotAttack(pathTraversalDetector.run("example/test.txt", new String[]{"/app/data/example/test.txt"}));
    }

    @Test
    public void testDoesNotAbsolutePathWithDifferentFolder() {
        assertNotAttack(pathTraversalDetector.run("/etc/hack/config", new String[]{"/etc/app/config"}));
    }

    @Test
    public void testDoesNotAbsolutePathInsideAnotherFolder() {
        assertNotAttack(pathTraversalDetector.run("/etc/config", new String[]{"/etc/app/data/etc/config"}));
    }
}
