package attack_wave_detection;

import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.vulnerabilities.attack_wave_detection.WebScanDetector;
import org.junit.jupiter.api.Test;
import utils.EmptySampleContextObject;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WebScanDetectorTest {

    private static ContextObject createTestContext(String path, String method, Map<String, List<String>> query) {
        return new EmptySampleContextObject(path, method, query);
    }

    @Test
    public void testIsWebScanner_PositiveCases() {
        // Test suspicious paths
        assertTrue(WebScanDetector.isWebScanner(createTestContext("/wp-config.php", "GET", Map.of())));
        assertTrue(WebScanDetector.isWebScanner(createTestContext("/.env", "GET", Map.of())));
        assertTrue(WebScanDetector.isWebScanner(createTestContext("/test/.env.bak", "GET", Map.of())));
        assertTrue(WebScanDetector.isWebScanner(createTestContext("/.git/config", "GET", Map.of())));
        assertTrue(WebScanDetector.isWebScanner(createTestContext("/.aws/config", "GET", Map.of())));
        assertTrue(WebScanDetector.isWebScanner(createTestContext("/../secret", "GET", Map.of())));

        // Test suspicious method
        assertTrue(WebScanDetector.isWebScanner(createTestContext("/", "BADMETHOD", Map.of())));

        // Test suspicious query parameters
        assertTrue(WebScanDetector.isWebScanner(
            createTestContext("/", "GET", Map.of("test", List.of("SELECT * FROM admin")))
        ));
        assertTrue(WebScanDetector.isWebScanner(
            createTestContext("/", "GET", Map.of("test", List.of("../etc/passwd")))
        ));
    }

    @Test
    public void testIsWebScanner_NegativeCases() {
        // Test safe paths
        assertFalse(WebScanDetector.isWebScanner(createTestContext("graphql", "POST", Map.of())));
        assertFalse(WebScanDetector.isWebScanner(createTestContext("/api/v1/users", "GET", Map.of())));
        assertFalse(WebScanDetector.isWebScanner(createTestContext("/public/index.html", "GET", Map.of())));
        assertFalse(WebScanDetector.isWebScanner(createTestContext("/static/js/app.js", "GET", Map.of())));
        assertFalse(WebScanDetector.isWebScanner(createTestContext("/uploads/image.png", "GET", Map.of())));

        // Test safe query parameters
        assertFalse(WebScanDetector.isWebScanner(
            createTestContext("/", "GET", Map.of("test", List.of("1'")))
        ));
        assertFalse(WebScanDetector.isWebScanner(
            createTestContext("/", "GET", Map.of("test", List.of("abcd")))
        ));
    }
}
