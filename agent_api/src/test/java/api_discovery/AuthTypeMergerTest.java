package api_discovery;

import org.junit.jupiter.api.Test;
import java.util.*;

import static dev.aikido.agent_api.api_discovery.AuthTypeMerger.mergeAuthTypes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AuthTypeMergerTest {

    @Test
    public void testMergeApiAuthTypes() {
        List<Map<String, String>> existing = new ArrayList<>();
        existing.add(createAuthType("http", "bearer", null, null));
        existing.add(createAuthType("apiKey", null, "x-api-key", "header"));

        List<Map<String, String>> newAuth = new ArrayList<>();
        newAuth.add(createAuthType("http", "bearer", null, null));
        newAuth.add(createAuthType("http", "basic", null, null));
        newAuth.add(createAuthType("apiKey", "header", "x-api-key-v2", null));

        List<Map<String, String>> expected = new ArrayList<>(existing);
        expected.add(createAuthType("http", "basic", null, null));
        expected.add(createAuthType("apiKey", "header", "x-api-key-v2", null));

        List<Map<String, String>> result = mergeAuthTypes(existing, newAuth);
        assertEquals(expected, result);

        // Test merging with null values
        assertNull(mergeAuthTypes(null, null));

        List<Map<String, String>> singleAuth = new ArrayList<>();
        singleAuth.add(createAuthType("http", "bearer", null, null));
        assertEquals(singleAuth, mergeAuthTypes(singleAuth, null));

        List<Map<String, String>> newSingleAuth = new ArrayList<>();
        newSingleAuth.add(createAuthType("http", "digest", null, null));
        assertEquals(newSingleAuth, mergeAuthTypes(null, newSingleAuth));
    }

    @Test
    public void testMergeApiAuthTypesWhereExistingNull() {
        List<Map<String, String>> newAuth = new ArrayList<>();
        newAuth.add(createAuthType("http", "bearer", null, null));
        newAuth.add(createAuthType("http", "basic", null, null));

        List<Map<String, String>> expected = new ArrayList<>(newAuth);

        List<Map<String, String>> result = mergeAuthTypes(null, newAuth);
        assertEquals(expected, result);

        result = mergeAuthTypes(new ArrayList<>(), newAuth);
        assertEquals(expected, result);
    }

    private Map<String, String> createAuthType(String type, String scheme, String name, String in) {
        Map<String, String> authType = new HashMap<>();
        authType.put("type", type);
        if (scheme != null) {
            authType.put("scheme", scheme);
        }
        if (name != null) {
            authType.put("name", name);
        }
        if (in != null) {
            authType.put("in", in);
        }
        return authType;
    }
}
