package api_discovery;

import dev.aikido.agent_api.api_discovery.GetAuthTypes;
import dev.aikido.agent_api.context.ContextObject;
import org.junit.jupiter.api.Test;
import utils.EmptySampleContextObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetAuthTypesTest {
    @Test
    void testGetAuthTypesWithNullHeaders() {
        ContextObject context = new EmptySampleContextObject((HashMap<String, List<String>>) null);

        List<Map<String, String>> result = GetAuthTypes.getAuthTypes(context);
        assertNull(result);
    }

    @Test
    void testGetAuthTypesWithEmptyHeaders() {
        ContextObject context = mock(ContextObject.class);
        when(context.getHeaders()).thenReturn(new HashMap<>());

        List<Map<String, String>> result = GetAuthTypes.getAuthTypes(context);
        assertNull(result);
    }

    @Test
    void testGetAuthTypesWithEmptyAuthHeader() {
        ContextObject context = mock(ContextObject.class);
        when(context.getHeaders()).thenReturn(new HashMap<>(Map.of("authorization", List.of(""))));

        List<Map<String, String>> result = GetAuthTypes.getAuthTypes(context);
        assertNull(result);
    }

    @Test
    void testGetAuthTypesWithAuthorizationHeader() {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("authorization", List.of("Bearer some_token"));
        ContextObject context = new EmptySampleContextObject(headers);

        List<Map<String, String>> result = GetAuthTypes.getAuthTypes(context);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("http", result.get(0).get("type"));
        assertEquals("bearer", result.get(0).get("scheme"));
    }

    @Test
    void testGetAuthTypesWithInvalidAuthorizationHeader() {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("authorization", List.of("InvalidScheme some_token"));
        ContextObject context = new EmptySampleContextObject(headers);

        List<Map<String, String>> result = GetAuthTypes.getAuthTypes(context);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("apiKey", result.get(0).get("type"));
        assertEquals("header", result.get(0).get("in"));
        assertEquals("Authorization", result.get(0).get("name"));
    }

    @Test
    void testGetAuthTypesWithInvalidAuthorizationHeader2() {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("authorization", List.of("InvalidScheme"));
        ContextObject context = new EmptySampleContextObject(headers);

        List<Map<String, String>> result = GetAuthTypes.getAuthTypes(context);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("apiKey", result.get(0).get("type"));
        assertEquals("header", result.get(0).get("in"));
        assertEquals("Authorization", result.get(0).get("name"));
    }

    @Test
    void testGetAuthTypesWithInvalidAuthorizationHeader3() {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("authorization", List.of("InvalidScheme some_token test2"));
        ContextObject context = new EmptySampleContextObject(headers);

        List<Map<String, String>> result = GetAuthTypes.getAuthTypes(context);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("apiKey", result.get(0).get("type"));
        assertEquals("header", result.get(0).get("in"));
        assertEquals("Authorization", result.get(0).get("name"));
    }

    @Test
    void testGetAuthTypesWithApiKeyInHeaders() {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("x-api-key", List.of("some_api_key"));
        ContextObject context = new EmptySampleContextObject(headers);

        List<Map<String, String>> result = GetAuthTypes.getAuthTypes(context);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("apiKey", result.get(0).get("type"));
        assertEquals("header", result.get(0).get("in"));
        assertEquals("x-api-key", result.get(0).get("name"));
    }

    @Test
    void testCookiesAreNull() {
        ContextObject context = mock(ContextObject.class);
        when(context.getHeaders()).thenReturn(new HashMap<>());
        when(context.getCookies()).thenReturn(null);

        List<Map<String, String>> result = GetAuthTypes.getAuthTypes(context);
        assertNull(result);
    }

    @Test
    void testGetAuthTypesWithApiKeyInCookies() {
        ContextObject context = mock(ContextObject.class);
        when(context.getHeaders()).thenReturn(new HashMap<>());

        HashMap<String, List<String>> cookies = new HashMap<>();
        cookies.put("auth", List.of("some_auth_cookie"));
        when(context.getCookies()).thenReturn(cookies);

        List<Map<String, String>> result = GetAuthTypes.getAuthTypes(context);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("apiKey", result.get(0).get("type"));
        assertEquals("cookie", result.get(0).get("in"));
        assertEquals("auth", result.get(0).get("name"));
    }
}
