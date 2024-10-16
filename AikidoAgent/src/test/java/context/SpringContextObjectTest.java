package context;

import dev.aikido.AikidoAgent.context.SpringContextObject;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

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
        Map<String, String[]> queryParams = springContextObject.getQuery();
        assertEquals(2, queryParams.size());
        assertArrayEquals(new String[]{"value1"}, queryParams.get("param1"));
        assertArrayEquals(new String[]{"value2"}, queryParams.get("param2"));
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
        Map<String, String> cookiesMap = springContextObject.getCookies();
        assertEquals(2, cookiesMap.size());
        assertEquals("abc123", cookiesMap.get("sessionId"));
        assertEquals("user1", cookiesMap.get("userId"));
    }
}
