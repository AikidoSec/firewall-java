package context;

import dev.aikido.agent_api.context.SpringWebfluxContextObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.InetSocketAddress;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SpringWebfluxContextObjectTest {

    private String method;
    private String uri;
    private InetSocketAddress rawIp;
    private HashMap<String, List<String>> cookies;
    private Map<String, List<String>> query;
    private Map<String, String> headerEntries;

    @BeforeEach
    void setUp() {
        method = "GET";
        uri = "/api/test";
        rawIp = new InetSocketAddress("192.168.1.1", 8080);
        cookies = new HashMap<>();
        query = new HashMap<>();
        headerEntries = new HashMap<>();
    }

    @Test
    void testConstructorWithValidInputs() {
        headerEntries.put("Content-Type", "application/json");
        cookies.put("sessionId", Collections.singletonList("abc123"));
        query.put("param", Collections.singletonList("value"));

        SpringWebfluxContextObject contextObject = new SpringWebfluxContextObject(
                method, uri, rawIp, cookies, query, headerEntries
        );

        assertEquals(method, contextObject.getMethod());
        assertEquals(uri, contextObject.getUrl());
        assertEquals(cookies, contextObject.getCookies());
        assertEquals(query, contextObject.getQuery());
        assertEquals("application/json", contextObject.getHeaders().get("content-type"));
        assertEquals("192.168.1.1", contextObject.getRemoteAddress());
        assertEquals("SpringWebflux", contextObject.getSource());
        assertNotNull(contextObject.getRoute());
        assertTrue(contextObject.getRedirectStartNodes().isEmpty());
    }

    @Test
    void testConstructorWithEmptyHeaders() {
        SpringWebfluxContextObject contextObject = new SpringWebfluxContextObject(
                method, uri, rawIp, cookies, query, new HashMap<>()
        );

        assertTrue(contextObject.getHeaders().isEmpty());
    }
}