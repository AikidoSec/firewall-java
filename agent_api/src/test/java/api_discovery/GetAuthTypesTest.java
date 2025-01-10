package api_discovery;

import dev.aikido.agent_api.api_discovery.GetAuthTypes;
import dev.aikido.agent_api.context.ContextObject;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetAuthTypesTest {
    @Test
    void testGetAuthTypesWithNullHeaders() {
        ContextObject context = mock(ContextObject.class);
        when(context.getHeaders()).thenReturn(null);

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
        when(context.getHeaders()).thenReturn(new HashMap<>(Map.of("authorization", "")));

        List<Map<String, String>> result = GetAuthTypes.getAuthTypes(context);
        assertNull(result);
    }

    @Test
    void testGetAuthTypesWithAuthorizationHeader() {
        ContextObject context = mock(ContextObject.class);
        HashMap<String, String> headers = new HashMap<>();
        headers.put("authorization", "Bearer some_token");
        when(context.getHeaders()).thenReturn(headers);

        List<Map<String, String>> result = GetAuthTypes.getAuthTypes(context);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("http", result.get(0).get("type"));
        assertEquals("bearer", result.get(0).get("scheme"));
    }

    @Test
    void testGetAuthTypesWithInvalidAuthorizationHeader() {
        ContextObject context = mock(ContextObject.class);
        HashMap<String, String> headers = new HashMap<>();
        headers.put("authorization", "InvalidScheme some_token");
        when(context.getHeaders()).thenReturn(headers);

        List<Map<String, String>> result = GetAuthTypes.getAuthTypes(context);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("apiKey", result.get(0).get("type"));
        assertEquals("header", result.get(0).get("in"));
        assertEquals("Authorization", result.get(0).get("name"));
    }

    @Test
    void testGetAuthTypesWithInvalidAuthorizationHeader2() {
        ContextObject context = mock(ContextObject.class);
        HashMap<String, String> headers = new HashMap<>();
        headers.put("authorization", "InvalidScheme");
        when(context.getHeaders()).thenReturn(headers);

        List<Map<String, String>> result = GetAuthTypes.getAuthTypes(context);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("apiKey", result.get(0).get("type"));
        assertEquals("header", result.get(0).get("in"));
        assertEquals("Authorization", result.get(0).get("name"));
    }

    @Test
    void testGetAuthTypesWithInvalidAuthorizationHeader3() {
        ContextObject context = mock(ContextObject.class);
        HashMap<String, String> headers = new HashMap<>();
        headers.put("authorization", "InvalidScheme some_token test2");
        when(context.getHeaders()).thenReturn(headers);

        List<Map<String, String>> result = GetAuthTypes.getAuthTypes(context);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("apiKey", result.get(0).get("type"));
        assertEquals("header", result.get(0).get("in"));
        assertEquals("Authorization", result.get(0).get("name"));
    }

    @Test
    void testGetAuthTypesWithApiKeyInHeaders() {
        ContextObject context = mock(ContextObject.class);
        HashMap<String, String> headers = new HashMap<>();
        headers.put("x-api-key", "some_api_key");
        when(context.getHeaders()).thenReturn(headers);

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
