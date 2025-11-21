package vulnerabilities.path_traversal;

import dev.aikido.agent_api.vulnerabilities.Detector;
import dev.aikido.agent_api.vulnerabilities.path_traversal.PathTraversalDetector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PathTraversalDetectorTest {
    private void assertNotAttack(Detector.DetectorResult detectorResult) {
        assertFalse(detectorResult.isDetectedAttack());
    }
    private void assertAttack(Detector.DetectorResult detectorResult) {
        assertTrue(detectorResult.isDetectedAttack());
    }
    @Test
    public void testEmptyUserInput() {
        assertNotAttack(PathTraversalDetector.INSTANCE.run("", new String[]{"test.txt"}));
    }

    @Test
    public void testEmptyFileInput() {
        assertNotAttack(PathTraversalDetector.INSTANCE.run("test", new String[]{""}));
    }

    @Test
    public void testEmptyUserInputAndFileInput() {
        assertNotAttack(PathTraversalDetector.INSTANCE.run("", new String[]{""}));
    }

    @Test
    public void testUserInputIsASingleCharacter() {
        assertNotAttack(PathTraversalDetector.INSTANCE.run("t", new String[]{"test.txt"}));
    }

    @Test
    public void testFileInputIsASingleCharacter() {
        assertNotAttack(PathTraversalDetector.INSTANCE.run("test", new String[]{"t"}));
    }

    @Test
    public void testSameAsUserInput() {
        assertNotAttack(PathTraversalDetector.INSTANCE.run("text.txt", new String[]{"text.txt"}));
    }

    @Test
    public void testWithDirectoryBefore() {
        assertNotAttack(PathTraversalDetector.INSTANCE.run("text.txt", new String[]{"directory/text.txt"}));
    }

    @Test
    public void testWithBothDirectoryBefore() {
        assertNotAttack(PathTraversalDetector.INSTANCE.run("directory/text.txt", new String[]{"directory/text.txt"}));
    }

    @Test
    public void testUserInputAndFileInputAreSingleCharacters() {
        assertNotAttack(PathTraversalDetector.INSTANCE.run("t", new String[]{"t"}));
    }

    @Test
    public void testItFlagsDotDotSlash() {
        assertAttack(PathTraversalDetector.INSTANCE.run("../", new String[]{"../test.txt"}));
    }

    @Test
    public void testItFlagsDotDotSlashSlash() {
        assertAttack(PathTraversalDetector.INSTANCE.run("..//", new String[]{"..//test.txt"}));
    }

    @Test
    public void testItFlagsMoreComplexPaths() {
        assertAttack(PathTraversalDetector.INSTANCE.run("..//secrets/key.txt", new String[]{"resources/blog/..//secrets/key.txt"}));
        assertAttack(PathTraversalDetector.INSTANCE.run(".././/secrets/key.txt", new String[]{"resources/blog/.././/secrets/key.txt"}));
        assertAttack(PathTraversalDetector.INSTANCE.run("////../secrets/key.txt", new String[]{"resources/blog/////../secrets/key.txt"}));
    }

    @Test
    public void testItFlagsBackslashDotDot() {
        assertAttack(PathTraversalDetector.INSTANCE.run("..\\", new String[]{"..\\test.txt",}));
    }

    @Test
    public void testItFlagsDoubleDotSlash() {
        assertAttack(PathTraversalDetector.INSTANCE.run("../../", new String[]{"../../test.txt"}));
    }

    @Test
    public void testItFlagsDoubleDotBackslash() {
        assertAttack(PathTraversalDetector.INSTANCE.run("..\\..\\", new String[]{"..\\..\\test.txt",}));
    }

    @Test
    public void testItFlagsFourDotSlash() {
        assertAttack(PathTraversalDetector.INSTANCE.run("../../../../", new String[]{"../../../../test.txt"}));
    }

    @Test
    public void testItFlagsThreeDotBackslash() {
        assertAttack(PathTraversalDetector.INSTANCE.run("..\\..\\..\\", new String[]{"..\\..\\..\\test.txt",}));
    }

    @Test
    public void testUserInputIsLongerThanFilePath() {
        assertNotAttack(PathTraversalDetector.INSTANCE.run("../../file.txt", new String[]{"../file.txt"}));
    }

    @Test
    public void testAbsoluteLinuxPath() {
        assertAttack(PathTraversalDetector.INSTANCE.run("/etc/passwd", new String[]{"/etc/passwd"}));
    }

    @Test
    public void testLinuxUserDirectory() {
        assertAttack(PathTraversalDetector.INSTANCE.run("/home/user/", new String[]{"/home/user/file.txt"}));
    }

    @Test
    public void testWindowsDriveLetter() {
        assertAttack(PathTraversalDetector.INSTANCE.run("C:\\", new String[]{"C:\\file.txt"}));
    }

    @Test
    public void testNoPathTraversal() {
        assertNotAttack(PathTraversalDetector.INSTANCE.run("/storage/file.txt", new String[]{"/appdata/storage/file.txt"}));
    }

    @Test
    public void testDoesNotFlagTest() {
        assertNotAttack(PathTraversalDetector.INSTANCE.run("test", new String[]{"/app/test.txt"}));
    }

    @Test
    public void testDoesNotFlagExampleTestTxt() {
        assertNotAttack(PathTraversalDetector.INSTANCE.run("example/test.txt", new String[]{"/app/data/example/test.txt"}));
    }

    @Test
    public void testDoesNotAbsolutePathWithDifferentFolder() {
        assertNotAttack(PathTraversalDetector.INSTANCE.run("/etc/hack/config", new String[]{"/etc/app/config"}));
    }

    @Test
    public void testDoesNotAbsolutePathInsideAnotherFolder() {
        assertNotAttack(PathTraversalDetector.INSTANCE.run("/etc/config", new String[]{"/etc/app/data/etc/config"}));
    }

    @Test
    public void testUserInputContainsMultipleUnsafePathParts() {
        assertNotAttack(PathTraversalDetector.INSTANCE.run("../../../", new String[]{"directory/../../file.txt"}));
    }

    @Test
    public void testUserInputWithMixedPathSeparators() {
        assertNotAttack(PathTraversalDetector.INSTANCE.run("..\\..\\", new String[]{"directory/../file.txt"}));
    }

    @Test
    public void testUserInputWithEncodedCharacters() {
        assertAttack(PathTraversalDetector.INSTANCE.run("%2E%2E/../", new String[]{"directory/%2E%2E/../file.txt"}));
    }

    @Test
    public void testUserInputWithEncodedBackslash() {
        assertNotAttack(PathTraversalDetector.INSTANCE.run("%5C%5C", new String[]{"directory/%5C%5Cfile.txt"}));
    }

    @Test
    public void testUserInputWithSpaces() {
        assertNotAttack(PathTraversalDetector.INSTANCE.run("test file", new String[]{"directory/test file.txt"}));
    }

    @Test
    public void testUserInputWithLeadingSpaces() {
        assertNotAttack(PathTraversalDetector.INSTANCE.run(" test.txt", new String[]{"directory/test.txt"}));
    }

    @Test
    public void testUserInputWithTrailingSpaces() {
        assertNotAttack(PathTraversalDetector.INSTANCE.run("test.txt ", new String[]{"directory/test.txt"}));
    }

    @Test
    public void testUserInputWithSpecialCharacters() {
        assertNotAttack(PathTraversalDetector.INSTANCE.run("test@file.txt", new String[]{"directory/test@file.txt"}));
    }

    @Test
    public void testUserInputWithAbsolutePath() {
        assertAttack(PathTraversalDetector.INSTANCE.run("/etc/passwd", new String[]{"/etc/passwd"}));
    }

    @Test
    public void testUserInputWithMixedCase() {
        assertNotAttack(PathTraversalDetector.INSTANCE.run("Test.txt", new String[]{"directory/test.txt"}));
    }

    @Test
    public void testUserInputWithLongPath() {
        String longUserInput = "a".repeat(260); // Assuming a long input
        assertNotAttack(PathTraversalDetector.INSTANCE.run(longUserInput, new String[]{"directory/test.txt"}));
    }

    @Test
    public void testUserInputWithEmptyFilePath() {
        assertNotAttack(PathTraversalDetector.INSTANCE.run("test", new String[]{""}));
    }

    @Test
    public void testUserInputWithFilePathContainingSpaces() {
        assertNotAttack(PathTraversalDetector.INSTANCE.run("test file", new String[]{"directory/test file.txt"}));
    }
}
