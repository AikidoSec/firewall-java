package attack_wave_detection;

import org.junit.jupiter.api.Test;

import static dev.aikido.agent_api.vulnerabilities.attack_wave_detection.MethodChecker.isWebScanMethod;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MethodCheckerTest {

    @Test
    void testIsWebScanMethod_WithBadMethods_ReturnsTrue() {
        String[] badMethods = {
            "BADMETHOD", "BADHTTPMETHOD", "BADDATA", "BADMTHD", "BDMTHD"
        };
        for (String method : badMethods) {
            assertTrue(
                isWebScanMethod(method),
                "Expected '" + method + "' to be detected as a web scan method"
            );
        }
    }

    @Test
    void testIsNotWebScanMethod_WithStandardMethods_ReturnsFalse() {
        String[] standardMethods = {
            "GET", "POST", "PUT", "DELETE", "PATCH",
            "OPTIONS", "HEAD", "TRACE", "CONNECT", "PURGE"
        };
        for (String method : standardMethods) {
            assertFalse(
                isWebScanMethod(method),
                "Expected '" + method + "' to NOT be detected as a web scan method"
            );
        }
    }
}
