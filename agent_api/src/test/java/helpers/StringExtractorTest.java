package helpers;

import org.junit.jupiter.api.Test;

import static dev.aikido.agent_api.helpers.extraction.StringExtractor.extractStringsFromObject;
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
                        "whaat2", ".age", "dangerous", ".user_input.[1]", "whaat", ".user_input.[0]")),
                extractStringsFromObject(Map.of("age", "whaat2", "user_input", new String[]{"whaat", "dangerous"})));
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
    @Test
    public void testDecodesJwts() {
        Map<String, String> input = new HashMap<>();
        input.put("token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwidXNlcm5hbWUiOnsiJG5lIjpudWxsfSwiaWF0IjoxNTE2MjM5MDIyfQ._jhGJw9WzB6gHKPSozTFHDo9NOHs3CNOlvJ8rWy6VrQ");
        Map<String, Object> map = new HashMap<String, Object>() {{
            put("token", ".");
            put("iat", ".token<jwt>");
            put("username", ".token<jwt>");
            put("sub", ".token<jwt>");
            put("1234567890", ".token<jwt>.sub");
            put("$ne", ".token<jwt>.username");
            put("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwidXNlcm5hbWUiOnsiJG5lIjpudWxsfSwiaWF0IjoxNTE2MjM5MDIyfQ._jhGJw9WzB6gHKPSozTFHDo9NOHs3CNOlvJ8rWy6VrQ", ".token");
        }};
        Map<String, String> expectedOutput = fromObj(map);

        assertEquals(expectedOutput, extractStringsFromObject(input));
    }

    @Test
    public void testIgnoresJwtIssuers() {
        Map<String, String> input = new HashMap<>();
        input.put("token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJpc3MiOiJodHRwczovL2V4YW1wbGUuY29tIn0.QLC0vl-A11a1WcUPD6vQR2PlUvRMsqpegddfQzPajQM");

        Map<String, String> expectedOutput = new HashMap<>();
        expectedOutput.put("token", ".");
        expectedOutput.put("iat", ".token<jwt>");
        expectedOutput.put("sub", ".token<jwt>");
        expectedOutput.put("1234567890", ".token<jwt>.sub");
        expectedOutput.put("name", ".token<jwt>");
        expectedOutput.put("John Doe", ".token<jwt>.name");
        expectedOutput.put("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJpc3MiOiJodHRwczovL2V4YW1wbGUuY29tIn0.QLC0vl-A11a1WcUPD6vQR2PlUvRMsqpegddfQzPajQM", ".token");

        assertEquals(expectedOutput, extractStringsFromObject(input));
    }

    @Test
    public void testJwtAsString() {
        Map<String, String> input = new HashMap<>();
        input.put("header", "/;ping%20localhost;.e30=.");

        Map<String, String> expectedOutput = fromObj(new HashMap<String, Object>() {{
            put("header", ".");
            put("/;ping%20localhost;.e30=.", ".header");
        }});

        assertEquals(expectedOutput, extractStringsFromObject(input));
    }

    @Test
    public void testExtractsStringsFromStringArray() {
        Map<String, Object> input = new HashMap<>();
        input.put("arr", new String[]{"1", "2", "3"});

        Map<String, String> expectedOutput = fromObj(new HashMap<String, Object>() {{
            put("arr", ".");
            put("1", ".arr.[0]");
            put("2", ".arr.[1]");
            put("3", ".arr.[2]");
        }});

        assertEquals(expectedOutput, extractStringsFromObject(input));
    }

    @Test
    public void testExtractsStringsFromMixedArray() {
        Map<String, Object> input = new HashMap<>();
        input.put("arr", new Object[]{"1", 2, true, null, new HashMap<String, String>() {{
            put("test", "test");
        }}});

        Map<String, String> expectedOutput = fromObj(new HashMap<String, Object>() {{
            put("arr", ".");
            put("1", ".arr.[0]");
            put("test", ".arr.[4].test");
        }});

        assertEquals(expectedOutput, extractStringsFromObject(input));
    }

    @Test
    public void testExtractsStringsFromMixedArrayContainingArray() {
        Map<String, Object> input = new HashMap<>();
        input.put("arr", new Object[]{"1", 2, true, null, new HashMap<String, Object>() {{
            put("test", new String[]{"test123", "test345"});
        }}});

        Map<String, String> expectedOutput = fromObj(new HashMap<String, Object>() {{
            put("arr", ".");
            put("1", ".arr.[0]");
            put("test", ".arr.[4]");
            put("test123", ".arr.[4].test.[0]");
            put("test345", ".arr.[4].test.[1]");
        }});

        assertEquals(expectedOutput, extractStringsFromObject(input));
    }
}
