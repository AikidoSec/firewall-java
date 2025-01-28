package context;

import dev.aikido.agent_api.context.NettyContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NettyContextTest {

    private String method;
    private String uri;
    private InetSocketAddress rawIp;
    private HashMap<String, List<String>> cookies;
    private Map<String, List<String>> query;
    private List<Map.Entry<String, String>> headerEntries;

    @BeforeEach
    void setUp() {
        method = "GET";
        uri = "/test/endpoint";
        rawIp = new InetSocketAddress("192.168.1.1", 8080);
        cookies = new HashMap<>();
        cookies.put("session", List.of("abc123"));
        query = new HashMap<>();
        query.put("param1", List.of("value1"));
        headerEntries = new ArrayList<>();
        headerEntries.add(Map.entry("Content-Type", "application/json"));
        headerEntries.add(Map.entry("User-Agent", "JUnit Test"));
    }

    @Test
    void testNettyContextConstructor() {
        NettyContext context = new NettyContext(method, uri, rawIp, cookies, query, headerEntries);

        assertEquals(method, context.getMethod());
        assertEquals(uri, context.getUrl());
        assertEquals(cookies, context.getCookies());
        assertEquals(query, context.getQuery());
        assertEquals("application/json", context.getHeaders().get("content-type"));
        assertEquals("JUnit Test", context.getHeaders().get("user-agent"));
        assertEquals("192.168.1.1", context.getRemoteAddress());
        assertEquals("ReactorNetty", context.getSource());
        assertNull(context.getParams());
        assertNotNull(context.getRedirectStartNodes());
        assertTrue(context.getRedirectStartNodes().isEmpty());
    }


    @Test
    void testRemoteAddressExtraction() {
        // Assuming getIpFromRequest is a static method that returns the IP address correctly
        // You may need to mock this method if it has complex logic
        headerEntries.add(Map.entry("X-FORWARDED-FOR", "192.168.2.1"));
        NettyContext context = new NettyContext(method, uri, rawIp, cookies, query, headerEntries);
        assertEquals("192.168.2.1", context.getRemoteAddress());
    }
}
