package helpers;

import api_discovery.DataSchemaGeneratorTest;
import dev.aikido.agent_api.api_discovery.DataSchemaGenerator;
import dev.aikido.agent_api.api_discovery.DataSchemaItem;
import dev.aikido.agent_api.api_discovery.DataSchemaType;
import org.junit.jupiter.api.Test;

import static dev.aikido.agent_api.helpers.extraction.StringExtractor.extractStringsFromObject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;

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

    @Test
    public void testExtractStringsFromArray() {
        Object input = List.of("test1", List.of("Test 2", "Test 3"));

        Map<String, String> expectedOutput = new HashMap<>();
        expectedOutput.put("Test 2", ".[1].[0]");
        expectedOutput.put("test1", ".[0]");
        expectedOutput.put("Test 3", ".[1].[1]");

        assertEquals(expectedOutput, extractStringsFromObject(input));
    }

    @Test
    public void testExtractStringsFromHashMap() {
        Map<String, Object> input = new HashMap<>();
        input.put("test", "abc");

        Map<String, String> expectedOutput = new HashMap<>();
        expectedOutput.put("abc", ".test");
        expectedOutput.put("test", ".");

        assertEquals(expectedOutput, extractStringsFromObject(input));
    }

    @Test
    public void testExtractStringsFromComplexObject() {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("test", 123);
        input.put("arr", Arrays.asList("Hello", 2, "World"));

        Map<String, String> expectedOutput = new HashMap<>();
        expectedOutput.put("arr", ".");
        expectedOutput.put("test", ".");
        expectedOutput.put("Hello", ".arr.[0]");
        expectedOutput.put("World", ".arr.[2]");

        assertEquals(expectedOutput, extractStringsFromObject(input));
    }

    @Test
    public void testExtractStringsFromNestedObject() {
        Map<String, Object> input = new HashMap<>();
        input.put("test", 123);
        input.put("arr", Arrays.asList(Collections.singletonMap("sub", true)));
        input.put("x", null);

        Map<String, String> expectedOutput = new HashMap<>();
        expectedOutput.put("arr", ".");
        expectedOutput.put("sub", ".arr.[0]");
        expectedOutput.put("test", ".");
        expectedOutput.put("x", ".");

        assertEquals(expectedOutput, extractStringsFromObject(input));
    }
    private record MyRecord(String a, Number abc, List<String> stringslist) {}
    @Test
    public void testExtractsFromClasses() {
        MyRecord myRecord = new MyRecord("Hello World", null, List.of("Abc", "def", "ghi"));
        Map<String, Object> input = new HashMap<>();
        input.put("important_record", myRecord);

        Map<String, String> expectedOutput = new HashMap<>();
        expectedOutput.put("Hello World", ".important_record.a");
        expectedOutput.put("Abc", ".important_record.stringslist.[0]");
        expectedOutput.put("def", ".important_record.stringslist.[1]");
        expectedOutput.put("important_record", ".");
        expectedOutput.put("ghi", ".important_record.stringslist.[2]");

        assertEquals(expectedOutput, extractStringsFromObject(input));
    }

    @Test
    void testUndefinedPrimitives() {
        Map<String, Object> input = new HashMap<>();
        input.put("character", 'a');

        Map<String, String> expectedOutput = new HashMap<>();
        expectedOutput.put("character", ".");

        assertEquals(expectedOutput, extractStringsFromObject(input));
    }

    @Test
    public void testExtractsFromMapWithNumericKeys() {
        Map<Object, Object> input = new HashMap<>();
        input.put(1, "one");
        input.put(2, "two");
        input.put(3, Arrays.asList("three", "four"));

        Map<String, String> expectedOutput = new HashMap<>();
        expectedOutput.put("one", ".1");
        expectedOutput.put("two", ".2");
        expectedOutput.put("three", ".3.[0]");
        expectedOutput.put("four", ".3.[1]");

        assertEquals(expectedOutput, extractStringsFromObject(input));
    }

    // Test with keys as instances of other classes
    private static class CustomKey {
        private final String key;

        public CustomKey(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    @Test
    public void testExtractsFromMapWithCustomKeyObjects() {
        Map<CustomKey, Object> input = new HashMap<>();
        input.put(new CustomKey("key1"), "value1");
        input.put(new CustomKey("key2"), Arrays.asList("value2a", "value2b"));

        Map<String, String> expectedOutput = new HashMap<>();
        expectedOutput.put("value1", ".?");
        expectedOutput.put("value2a", ".?.[0]");
        expectedOutput.put("value2b", ".?.[1]");

        assertEquals(expectedOutput, extractStringsFromObject(input));
    }

    // Test with nested maps containing numeric keys
    @Test
    public void testExtractsFromNestedMapWithNumericKeys() {
        Map<Object, Object> input = new HashMap<>();
        Map<Object, Object> nestedMap = new HashMap<>();
        nestedMap.put(1, "nestedValue1");
        nestedMap.put(2, "nestedValue2");
        input.put(0, nestedMap);

        Map<String, String> expectedOutput = new HashMap<>();
        expectedOutput.put("nestedValue1", ".0.1");
        expectedOutput.put("nestedValue2", ".0.2");

        assertEquals(expectedOutput, extractStringsFromObject(input));
    }

    // Test with mixed types in a map
    @Test
    public void testExtractsFromMapWithMixedTypes() {
        Map<Object, Object> input = new HashMap<>();
        input.put("stringKey", "stringValue");
        input.put(100, 200);
        input.put("listKey", Arrays.asList("item1", 2, true));

        Map<String, String> expectedOutput = new HashMap<>();
        expectedOutput.put("stringValue", ".stringKey");
        expectedOutput.put("item1", ".listKey.[0]");
        expectedOutput.put("stringKey", ".");
        expectedOutput.put("listKey", ".");

        assertEquals(expectedOutput, extractStringsFromObject(input));
    }

    // Test with a map containing other maps as values
    @Test
    public void testExtractsFromMapWithMapValues() {
        Map<String, Object> input = new HashMap<>();
        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("innerKey1", "innerValue1");
        innerMap.put("innerKey2", "innerValue2");
        input.put("outerKey", innerMap);

        Map<String, String> expectedOutput = new HashMap<>();
        expectedOutput.put("innerValue1", ".outerKey.innerKey1");
        expectedOutput.put("innerValue2", ".outerKey.innerKey2");
        expectedOutput.put("outerKey", ".");
        expectedOutput.put("innerKey2", ".outerKey");
        expectedOutput.put("innerKey1", ".outerKey");

        assertEquals(expectedOutput, extractStringsFromObject(input));
    }

    @Test
    public void testExtractString() {

        Map<String, String> expectedOutput = new HashMap<>();
        expectedOutput.put("my_string", ".");
        assertEquals(expectedOutput, extractStringsFromObject("my_string"));
    }
}
