package context;

import dev.aikido.agent_api.context.RingContextObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class RingContextObjectTest {

    private RingContextObject contextObject;

    @BeforeEach
    void setUp() {
        contextObject = new RingContextObject(
                "GET", new StringBuffer("http://localhost/test"), "192.168.1.1", Map.of(), new HashMap<>(), new HashMap<>(), null
        );
    }

    @Test
    void testConstructor() {
        assertEquals("GET", contextObject.getMethod());
        assertEquals("http://localhost/test", contextObject.getUrl());
        assertEquals("192.168.1.1", contextObject.getRemoteAddress());
        assertEquals("Ring", contextObject.getSource());
    }

    @Test
    void testGetRouteWithSlash() {
        contextObject = new RingContextObject(
                "GET", new StringBuffer("http://localhost/test"), "192.168.1.1", Map.of(), new HashMap<>(), new HashMap<>(), "a=b"
        );

        assertEquals("http://localhost/test?a=b", contextObject.getUrl());
        assertEquals("/test", contextObject.getRoute());
    }

    @Test
    void testGetRouteWithNumbers() {
        contextObject = new RingContextObject(
                "GET", new StringBuffer("http://localhost/api/dog/28632"), "192.168.1.1", Map.of(), new HashMap<>(), new HashMap<>(), ""
        );

        assertEquals("http://localhost/api/dog/28632", contextObject.getUrl());
        assertEquals("/api/dog/:number", contextObject.getRoute());
    }

    @Test
    void testQueryParametersExtraction() {
        Map<String, String[]> queryParams = new HashMap<>();
        queryParams.put("param1", new String[]{"value1"});

        contextObject = new RingContextObject(
                "GET", new StringBuffer("http://localhost/test"), "192.168.1.1", queryParams, new HashMap<>(), new HashMap<>(), null
        );

        assertEquals(1, contextObject.getQuery().size());
        assertEquals("value1", contextObject.getQuery().get("param1").get(0));
    }

    @Test
    void testCookiesExtraction() {
        HashMap<String, List<String>> cookies = new HashMap<>();
        cookies.put("sessionId", List.of("abc123"));

        contextObject = new RingContextObject(
                "GET", new StringBuffer("http://localhost/test"), "192.168.1.1", Map.of(), cookies, new HashMap<>(), null
        );

        assertEquals(1, contextObject.getCookies().size());
        assertEquals("abc123", contextObject.getCookies().get("sessionId").get(0));
    }

    @Test
    void testHeadersExtraction() {
        Vector<String> contentTypeValues = new Vector<>(List.of("application/json"));
        HashMap<String, Enumeration<String>> headers = new HashMap<>();
        headers.put("Content-Type", contentTypeValues.elements());

        contextObject = new RingContextObject(
                "GET", new StringBuffer("http://localhost/test"), "192.168.1.1", Map.of(), new HashMap<>(), headers, null
        );

        assertEquals("application/json", contextObject.getHeader("content-type"));
    }
}
