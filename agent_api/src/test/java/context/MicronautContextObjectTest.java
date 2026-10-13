package context;

import dev.aikido.agent_api.context.MicronautContextObject;
import dev.aikido.agent_api.context.RouteMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MicronautContextObjectTest {

    private MicronautContextObject contextObject;

    @BeforeEach
    void setUp() {
        Map<String, List<String>> query = new HashMap<>();
        query.put("param1", List.of("value1"));
        HashMap<String, List<String>> cookies = new HashMap<>();
        cookies.put("sessionId", List.of("abc123", "456"));
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Content-Type", List.of("application/json"));

        contextObject = new MicronautContextObject(
                "GET", "http://example.com/api/resource", "192.168.1.1", query, cookies, headers);
    }

    @Test
    void testConstructor() {
        assertEquals("GET", contextObject.getMethod());
        assertEquals("http://example.com/api/resource", contextObject.getUrl());
        assertEquals("192.168.1.1", contextObject.getRemoteAddress());
        assertEquals("Micronaut", contextObject.getSource());
        assertEquals("value1", contextObject.getQuery().get("param1").get(0));
    }

    @Test
    void testHeadersLowercased() {
        assertEquals("application/json", contextObject.getHeader("content-type"));
        assertTrue(contextObject.getHeaders().containsKey("content-type"));
    }

    @Test
    void testCookiesExtraction() {
        assertEquals("abc123", contextObject.getCookies().get("sessionId").get(0));
        assertEquals("456", contextObject.getCookies().get("sessionId").get(1));
    }

    @Test
    void testFullBodyOverridesBodyMap() {
        contextObject.setBodyElement("field", "partial");
        Object fullBody = new HashMap<String, String>() {{ put("all", "data"); }};
        contextObject.setBody(fullBody);
        assertEquals(fullBody, contextObject.getBody());
    }

    @Test
    void testBodyMapUsedWhenNoFullBody() {
        contextObject.setBodyElement("field", "data");
        assertTrue(contextObject.getBody() instanceof Map);
        assertEquals("data", ((Map<?, ?>) contextObject.getBody()).get("field"));
    }

    @Test
    void testPathVariablesAsParams() {
        contextObject.setParameter("id", "42");
        assertTrue(contextObject.getParams() instanceof Map);
        assertEquals("42", ((Map<?, ?>) contextObject.getParams()).get("id"));
    }

    @Test
    void testGetRouteMetadata() {
        RouteMetadata metadata = contextObject.getRouteMetadata();
        assertNotNull(metadata);
        assertEquals(contextObject.getRoute(), metadata.route());
        assertEquals(contextObject.getMethod(), metadata.method());
    }

    @Test
    void testConstructorStripsPortFromRawIp() {
        MicronautContextObject ctx = new MicronautContextObject(
                "GET", "http://example.com", "109.132.232.101:58780",
                new HashMap<>(), new HashMap<>(), new HashMap<>());
        assertEquals("109.132.232.101", ctx.getRemoteAddress());
    }
}
