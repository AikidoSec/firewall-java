package context;

import dev.aikido.agent_api.context.SpringContextObject;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SpringContextObjectTest {

    private HttpServletRequest request;
    private SpringContextObject springContextObject;

    @BeforeEach
    void setUp() {
        request = Mockito.mock(HttpServletRequest.class);
    }

    @Test
    void testInitialization() {
        // Arrange
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/test"));
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        // Act
        springContextObject = new SpringContextObject(request);

        // Assert
        assertEquals("GET", springContextObject.getMethod());
        assertEquals("http://localhost/test", springContextObject.getUrl());
        assertEquals("192.168.1.1", springContextObject.getRemoteAddress());
        assertEquals("SpringFramework", springContextObject.getSource());
        assertNull(springContextObject.getParams());
    }

    @Test
    void testHeaderExtraction() {
        // Arrange
        when(request.getHeaderNames()).thenReturn(
                new Enumeration<String>() {
                    private final String[] headers = {"Content-Type", "Authorization"};
                    private int index = 0;

                    @Override
                    public boolean hasMoreElements() {
                        return index < headers.length;
                    }

                    @Override
                    public String nextElement() {
                        return headers[index++];
                    }
                }
        );
        when(request.getHeader("Content-Type")).thenReturn("application/json");
        when(request.getHeader("Authorization")).thenReturn("Bearer token");

        // Act
        springContextObject = new SpringContextObject(request);

        // Assert
        Map<String, String> headers = springContextObject.getHeaders();
        assertEquals(2, headers.size());
        assertEquals("application/json", headers.get("Content-Type"));
        assertEquals("Bearer token", headers.get("Authorization"));
    }

    @Test
    void testQueryParameterExtraction() {
        // Arrange
        when(request.getParameterMap()).thenReturn(new HashMap<String, String[]>() {{
            put("param1", new String[]{"value1"});
            put("param2", new String[]{"value2"});
        }});

        // Act
        springContextObject = new SpringContextObject(request);

        // Assert
        Map<String, List<String>> queryParams = springContextObject.getQuery();
        assertEquals(2, queryParams.size());
        assertArrayEquals(new String[]{"value1"}, queryParams.get("param1").toArray());
        assertArrayEquals(new String[]{"value2"}, queryParams.get("param2").toArray());
    }

    @Test
    void testCookieExtraction() {
        // Arrange
        Cookie[] cookies = new Cookie[]{
                new Cookie("sessionId", "abc123"),
                new Cookie("userId", "user1")
        };
        when(request.getCookies()).thenReturn(cookies);

        // Act
        springContextObject = new SpringContextObject(request);

        // Assert
        Map<String, List<String>> cookiesMap = springContextObject.getCookies();
        assertEquals(2, cookiesMap.size());
        assertArrayEquals(new String[]{"abc123"}, cookiesMap.get("sessionId").toArray());
        assertArrayEquals(new String[]{"user1"}, cookiesMap.get("userId").toArray());
    }
    @Test
    void testGetRouteWithSlashTest() {
        // Arrange
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/test"));
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        // Act
        springContextObject = new SpringContextObject(request);

        // Assert
        assertEquals("http://localhost/test", springContextObject.getUrl());
        assertEquals("/test", springContextObject.getRoute());
    }
    @Test
    void testGetRouteWithNumbers() {
        // Arrange
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/dog/28632"));
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        // Act
        springContextObject = new SpringContextObject(request);

        // Assert
        assertEquals("http://localhost/api/dog/28632", springContextObject.getUrl());
        assertEquals("/api/dog/:number", springContextObject.getRoute());
    }

    @Test
    void testSetParams() {
        springContextObject = new SpringContextObject(request);
        springContextObject.setParams("12345");
        assertEquals("12345", springContextObject.getParams());
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TRUST_PROXY", value = "1")
    void testIpRequestFeature() {
        // Arrange
        Vector<String> headerVector = new Vector<>(List.of("x-forwarded-for"));
        when(request.getHeaderNames()).thenReturn(headerVector.elements());
        when(request.getHeader("x-forwarded-for")).thenReturn("invalid.ip, in.va.li.d, 1.2.3.4");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        springContextObject = new SpringContextObject(request);

        assertEquals(1, springContextObject.getHeaders().size());
        assertEquals("1.2.3.4", springContextObject.getRemoteAddress());
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TRUST_PROXY", value = "1")
    void testIpRequestFeature_InvalidHeader() {
        // Arrange
        Vector<String> headerVector = new Vector<>(List.of("x-forwarded-for"));
        when(request.getHeaderNames()).thenReturn(headerVector.elements());
        when(request.getHeader("x-forwarded-for")).thenReturn("invalid.ip, in.va.li.d");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        springContextObject = new SpringContextObject(request);

        assertEquals(1, springContextObject.getHeaders().size());
        assertEquals("192.168.1.1", springContextObject.getRemoteAddress());
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TRUST_PROXY", value = "0")
    void testIpRequestFeature_TrustProxyOff() {
        // Arrange
        Vector<String> headerVector = new Vector<>(List.of("x-forwarded-for"));
        when(request.getHeaderNames()).thenReturn(headerVector.elements());
        when(request.getHeader("x-forwarded-for")).thenReturn("invalid.ip, in.va.li.d, 1.2.3.4");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        springContextObject = new SpringContextObject(request);

        assertEquals(1, springContextObject.getHeaders().size());
        assertEquals("192.168.1.1", springContextObject.getRemoteAddress());
    }
}
