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
            "/../example/", "/./test", "/Cargo.lock", "/System32/test",
        };
        for (String path : dangerousPaths) {
            assertTrue(
                isWebScanPath(path, 404),
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
                isWebScanPath(path, 404),
                "Expected '" + path + "' to NOT be detected as a web scan path"
            );
        }
    }

    @Test
    void testForeignExtensions_404_ReturnsTrue() {
        assertTrue(isWebScanPath("/admin.php", 404),
            "php extension with 404 should be a scan path");
        assertTrue(isWebScanPath("/config.php5", 404),
            "php5 extension with 404 should be a scan path");
        assertTrue(isWebScanPath("/index.php3", 404),
            "php3 extension with 404 should be a scan path");
        assertTrue(isWebScanPath("/index.php4", 404),
            "php4 extension with 404 should be a scan path");
        assertTrue(isWebScanPath("/page.phtml", 404),
            "phtml extension with 404 should be a scan path");
    }

    @Test
    void testForeignExtensions_200_ReturnsFalse() {
        assertFalse(isWebScanPath("/admin.php", 200),
            "php extension with 200 should NOT be a scan path (app may proxy to PHP backend)");
        assertFalse(isWebScanPath("/config.php5", 200),
            "php5 extension with 200 should NOT be a scan path");
        assertFalse(isWebScanPath("/page.phtml", 200),
            "phtml extension with 200 should NOT be a scan path");
    }
}
