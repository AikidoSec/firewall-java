package helpers;

import dev.aikido.AikidoAgent.helpers.extraction.StringExtractor;
import org.junit.jupiter.api.Test;

import static dev.aikido.AikidoAgent.helpers.extraction.StringExtractor.extractStringsFromObject;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

public class StringExtractorTest {
    private static Map<String, String> fromObj(Map<String, Object> obj) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : obj.entrySet()) {
            result.put(entry.getKey(), (String) entry.getValue());
        }
        return result;
    }

    @Test
    public void testEmptyObjectReturnsEmptyDict() {
        assertEquals(fromObj(new HashMap<>()), extractStringsFromObject(new HashMap<>()));
    }

    @Test
    public void testExtractQueryObjects() {
        assertEquals(fromObj(Map.of("age", ".", "$gt", ".age", "21", ".age.$gt")),
                extractStringsFromObject(Map.of("age", Map.of("$gt", "21"))));
        assertEquals(fromObj(Map.of("title", ".", "$ne", ".title", "null", ".title.$ne")),
                extractStringsFromObject(Map.of("title", Map.of("$ne", "null"))));
        assertEquals(fromObj(Map.of("user_input", ".", "age", ".",
                        "whaat", ".age", "dangerous", ".user_input.[1]")),
                extractStringsFromObject(Map.of("age", "whaat", "user_input", new String[]{"whaat", "dangerous"})));
    }

    @Test
    public void testExtractCookieObjects() {
        assertEquals(fromObj(Map.of("session2", ".", "session", ".", "ABC", ".session", "DEF", ".session2")),
                extractStringsFromObject(Map.of("session", "ABC", "session2", "DEF")));
        assertEquals(fromObj(Map.of("session2", ".", "session", ".", "ABC", ".session")),
                extractStringsFromObject(Map.of("session", "ABC", "session2", 1234)));
    }

    @Test
    public void testExtractHeaderObjects() {
        assertEquals(fromObj(Map.of("Content-Type", ".", "application/json", ".Content-Type")),
                extractStringsFromObject(Map.of("Content-Type", "application/json")));
        assertEquals(fromObj(Map.of("Content-Type", ".")),
                extractStringsFromObject(Map.of("Content-Type", 54321)));
        assertEquals(fromObj(Map.of("Content-Type", ".", "application/json", ".Content-Type", "ExtraHeader", ".", "value", ".ExtraHeader")),
                extractStringsFromObject(Map.of("Content-Type", "application/json", "ExtraHeader", "value")));
    }

    @Test
    public void testExtractBodyObjects() {
        assertEquals(fromObj(Map.of("nested", ".nested", "$ne", ".nested.nested")),
                extractStringsFromObject(Map.of("nested", Map.of("nested", Map.of("$ne", true)))));
        assertEquals(fromObj(Map.of("age", ".", "$lt", ".age", "$gt", ".age", "21", ".age.$gt", "100", ".age.$lt")),
                extractStringsFromObject(Map.of("age", Map.of("$gt", "21", "$lt", "100"))));
    }
}
