package context;

import dev.aikido.agent_api.context.JavalinContextObject;
import dev.aikido.agent_api.context.RouteMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class JavalinContextObjectTest {

    private JavalinContextObject contextObject;

    @BeforeEach
    void setUp() {
        String method = "GET";
        String url = "http://example.com/api/resource";
        String rawIp = "192.168.1.1";
        Map<String, List<String>> queryParams = new HashMap<>();
        queryParams.put("param1", List.of("value1"));
        HashMap<String, List<String>> cookies = new HashMap<>();
        cookies.put("sessionId", List.of("abc123", "456"));
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        contextObject = new JavalinContextObject(method, url, rawIp, queryParams, cookies, headers);
    }

    @Test
    void testConstructor() {
        assertEquals("GET", contextObject.getMethod());
        assertEquals("http://example.com/api/resource", contextObject.getUrl());
        assertEquals("192.168.1.1", contextObject.getRemoteAddress());
        assertEquals("application/json", contextObject.getHeader("content-type"));
        assertEquals(1, contextObject.getQuery().size());
        assertEquals("value1", contextObject.getQuery().get("param1").get(0));
        assertEquals(1, contextObject.getCookies().size());
        assertEquals("abc123", contextObject.getCookies().get("sessionId").get(0));
        assertEquals("456", contextObject.getCookies().get("sessionId").get(1));

    }

    @Test
    void testSetParams() {
        Object params = new HashMap<String, String>() {{
            put("key", "value");
        }};
        contextObject.setParams(params);
        assertEquals(params, contextObject.getParams());
    }

    @Test
    void testSetBody() {
        Object body = new HashMap<String, String>() {{
            put("field", "data");
        }};
        contextObject.setBody(body);
        assertEquals(body, contextObject.getBody());
    }

    @Test
    void testSetExecutedMiddleware() {
        contextObject.setExecutedMiddleware(true);
        assertTrue(contextObject.middlewareExecuted());
    }

    @Test
    void testGetRouteMetadata() {
        RouteMetadata metadata = contextObject.getRouteMetadata();
        assertNotNull(metadata);
        assertEquals(contextObject.getRoute(), metadata.route());
        assertEquals(contextObject.getUrl(), metadata.url());
        assertEquals(contextObject.getMethod(), metadata.method());
    }

    @Test
    void testHeadersExtraction() {
        // Test headers extraction through the constructor
        assertEquals("application/json", contextObject.getHeader("content-type"));
        assertEquals(1, contextObject.getHeaders().size());
        assertTrue(contextObject.getHeaders().containsKey("content-type"));
    }

    @Test
    void testCookiesExtraction() {
        // Test cookies extraction through the constructor
        assertEquals(1, contextObject.getCookies().size());
        assertTrue(contextObject.getCookies().containsKey("sessionId"));
        assertEquals("abc123", contextObject.getCookies().get("sessionId").get(0));
    }

    @Test
    void testMultipleCookiesExtraction() {
        // Test with multiple cookies
        HashMap<String, List<String>> cookies = new HashMap<>();
        cookies.put("sessionId", List.of("abc123"));
        cookies.put("userId", List.of("user456"));
        contextObject = new JavalinContextObject("GET", "http://example.com", "192.168.1.1", new HashMap<>(), cookies, new HashMap<>());

        assertEquals("abc123", contextObject.getCookies().get("sessionId").get(0));
        assertEquals("user456", contextObject.getCookies().get("userId").get(0));
        assertEquals(2, contextObject.getCookies().size());
    }
}
