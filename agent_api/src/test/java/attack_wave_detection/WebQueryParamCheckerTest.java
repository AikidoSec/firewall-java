package attack_wave_detection;

import dev.aikido.agent_api.vulnerabilities.attack_wave_detection.WebQueryParamChecker;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static dev.aikido.agent_api.vulnerabilities.attack_wave_detection.WebQueryParamChecker.queryParamsContainDangerousPayload;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WebQueryParamCheckerTest {
    @Test
    void testQueryParamsContainDangerousPayload_WithDangerousPayload_ReturnsTrue() {
        HashMap<String, List<String>> queryStrings = new HashMap<>();
        queryStrings.put("q", List.of("SELECT * FROM users"));
        assertTrue(queryParamsContainDangerousPayload(queryStrings));
    }

    @Test
    void testQueryParamsContainDangerousPayload_WithSafePayload_ReturnsFalse() {
        HashMap<String, List<String>> queryStrings = new HashMap<>();
        queryStrings.put("q", List.of("hello world"));
        assertFalse(WebQueryParamChecker.queryParamsContainDangerousPayload(queryStrings));
    }

    @Test
    void testQueryParamsContainDangerousPayload_WithMixedPayload_ReturnsTrue() {
        HashMap<String, List<String>> queryStrings = new HashMap<>();
        queryStrings.put("q", List.of("safe", "1'='1"));
        assertTrue(WebQueryParamChecker.queryParamsContainDangerousPayload(queryStrings));
    }

    @Test
    void testQueryParamsContainDangerousPayload_WithShortPayload_ReturnsFalse() {
        HashMap<String, List<String>> queryStrings = new HashMap<>();
        queryStrings.put("q", List.of("hi"));
        assertFalse(WebQueryParamChecker.queryParamsContainDangerousPayload(queryStrings));
    }

    @Test
    void testQueryParamsContainDangerousPayload_WithLongPayload_ReturnsFalse() {
        HashMap<String, List<String>> queryStrings = new HashMap<>();
        String longPayload = "a".repeat(1001);
        queryStrings.put("q", List.of(longPayload));
        assertFalse(WebQueryParamChecker.queryParamsContainDangerousPayload(queryStrings));
    }

    @Test
    void testQueryParamsContainDangerousPayload_WithNullInput_ReturnsFalse() {
        assertFalse(WebQueryParamChecker.queryParamsContainDangerousPayload(null));
    }

    @Test
    void testQueryParamsContainDangerousPayload_WithEmptyInput_ReturnsFalse() {
        HashMap<String, List<String>> queryStrings = new HashMap<>();
        assertFalse(WebQueryParamChecker.queryParamsContainDangerousPayload(queryStrings));
    }

    @Test
    void testQueryParamsContainDangerousPayload_WithCaseInsensitiveMatch_ReturnsTrue() {
        HashMap<String, List<String>> queryStrings = new HashMap<>();
        queryStrings.put("q", List.of("select * from users"));
        assertTrue(WebQueryParamChecker.queryParamsContainDangerousPayload(queryStrings));
    }

    @Test
    void testQueryParamsContainDangerousPayload_WithPathTraversal_ReturnsTrue() {
        HashMap<String, List<String>> queryStrings = new HashMap<>();
        queryStrings.put("q", List.of("../../../etc/passwd"));
        assertTrue(WebQueryParamChecker.queryParamsContainDangerousPayload(queryStrings));
    }

    @Test
    void testDetectsInjectionPatterns() {
        String[] testStrings = {
            "' or '1'='1",
            "1: SELECT * FROM users WHERE '1'='1'",
            "', information_schema.tables",
            "1' sleep(5)",
            "WAITFOR DELAY 1",
            "../etc/passwd"
        };
        for (String str : testStrings) {
            HashMap<String, List<String>> queryStrings = new HashMap<>();
            queryStrings.put("test", List.of(str));
            queryStrings.put("utmSource", List.of("newsletter"));
            queryStrings.put("utmMedium", List.of("electronicmail"));
            queryStrings.put("utmCampaign", List.of("test"));
            queryStrings.put("utmTerm", List.of("sql_injection"));
            assertTrue(
                WebQueryParamChecker.queryParamsContainDangerousPayload(queryStrings),
                "Expected '" + str + "' to match patterns"
            );
        }
    }

    @Test
    void testDoesNotDetectSafePatterns() {
        String[] nonMatchingQueryElements = {
            "google.de", "some-string", "1", ""
        };
        for (String str : nonMatchingQueryElements) {
            HashMap<String, List<String>> queryStrings = new HashMap<>();
            queryStrings.put("test", List.of(str));
            assertFalse(
                WebQueryParamChecker.queryParamsContainDangerousPayload(queryStrings),
                "Expected '" + str + "' to NOT match patterns"
            );
        }
    }

    @Test
    void testHandlesEmptyQueryObject() {
        HashMap<String, List<String>> emptyQuery = new HashMap<>();
        assertFalse(
            WebQueryParamChecker.queryParamsContainDangerousPayload(emptyQuery),
            "Expected empty query to NOT match injection patterns"
        );
    }

    @Test
    void testHandlesNullQueryObject() {
        assertFalse(
            WebQueryParamChecker.queryParamsContainDangerousPayload(null),
            "Expected null query to NOT match injection patterns"
        );
    }
}
