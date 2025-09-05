package attack_wave_detection;

import org.junit.jupiter.api.Test;

import static dev.aikido.agent_api.vulnerabilities.attack_wave_detection.PathChecker.isWebScanPath;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PathCheckerTest {

    @Test
    void testIsWebScanPath_WithDangerousPaths_ReturnsTrue() {
        String[] dangerousPaths = {
            "/.env", "/test/.env", "/test/.env.bak", "/.git/config",
            "/.aws/config", "/some/path/.git/test", "/some/path/.gitlab-ci.yml",
            "/some/path/.github/workflows/test.yml", "/.travis.yml",
            "/../example/", "/./test"
        };
        for (String path : dangerousPaths) {
            assertTrue(
                isWebScanPath(path),
                "Expected '" + path + "' to be detected as a web scan path"
            );
        }
    }

    @Test
    void testIsNotWebScanPath_WithSafePaths_ReturnsFalse() {
        String[] safePaths = {
            "/test/file.txt", "/some/route/to/file.txt", "/some/route/to/file.json",
            "/en", "/", "/test/route", "/static/file.css", "/static/file.a461f56e.js"
        };
        for (String path : safePaths) {
            assertFalse(
                isWebScanPath(path),
                "Expected '" + path + "' to NOT be detected as a web scan path"
            );
        }
    }
}
