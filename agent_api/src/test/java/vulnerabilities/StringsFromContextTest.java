package vulnerabilities;

import dev.aikido.agent_api.context.SpringContextObject;
import dev.aikido.agent_api.vulnerabilities.StringsFromContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.Serializable;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class StringsFromContextTest {
    private HttpServletRequest request;
    private SpringContextObject springContextObject;

    @BeforeEach
    void setUp() {

        request = Mockito.mock(HttpServletRequest.class);
        // Arrange
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/test"));
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
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
        when(request.getParameterMap()).thenReturn(new HashMap<String, String[]>() {{
            put("param1", new String[]{"value1"});
            put("param2", new String[]{"value2"});
        }});
        Cookie[] cookies = new Cookie[]{
                new Cookie("sessionId", "abc123"),
                new Cookie("userId", "user1")
        };
        when(request.getCookies()).thenReturn(cookies);

        // Act
        springContextObject = new SpringContextObject(request);
    }

    @Test
    public void testExtractsFromRequest() {
        Map<String, Map<String, String>> strings = new StringsFromContext(springContextObject).getAll();
        assertEquals(Map.of(
                "body", Map.of(),
                "headers", Map.of(
                        "Content-Type", ".",
                        "Authorization", ".",
                        "application/json", ".Content-Type",
                        "Bearer token", ".Authorization"),
                "cookies", Map.of(
                        "user1", ".userId",
                        "abc123", ".sessionId",
                        "sessionId", ".", "userId", "."),
                "routeParams", Map.of(),
                "query", Map.of(
                        "value2", ".param2.[0]",
                        "value1", ".param1.[0]",
                        "param1", ".",
                        "param2", ".")
        ), strings);
    }

    @Test
    public void testExtractsFromRequestAndBody() {
        springContextObject.setBody((Serializable) List.of("1", "20", "2"));
        Map<String, Map<String, String>> strings = new StringsFromContext(springContextObject).getAll();
        assertEquals(Map.of(
                "body", Map.of("1", ".[0]", "2", ".[2]", "20",".[1]"),
                "headers", Map.of(
                        "Content-Type", ".",
                        "Authorization", ".",
                        "application/json", ".Content-Type",
                        "Bearer token", ".Authorization"),
                "cookies", Map.of(
                        "user1", ".userId",
                        "abc123", ".sessionId",
                        "sessionId", ".", "userId", "."),
                "routeParams", Map.of(),
                "query", Map.of(
                        "value2", ".param2.[0]",
                        "value1", ".param1.[0]",
                        "param1", ".",
                        "param2", ".")
        ), strings);
    }
    @Test
    public void testExtractsFromRequestAndRouteParams() {
        springContextObject.setParams((Serializable) List.of("1", "20", "2"));
        Map<String, Map<String, String>> strings = new StringsFromContext(springContextObject).getAll();
        assertEquals(Map.of(
                "body", Map.of(),
                "headers", Map.of(
                        "Content-Type", ".",
                        "Authorization", ".",
                        "application/json", ".Content-Type",
                        "Bearer token", ".Authorization"),
                "cookies", Map.of(
                        "user1", ".userId",
                        "abc123", ".sessionId",
                        "sessionId", ".", "userId", "."),
                "routeParams", Map.of("1", ".[0]", "2", ".[2]", "20",".[1]"),
                "query", Map.of(
                        "value2", ".param2.[0]",
                        "value1", ".param1.[0]",
                        "param1", ".",
                        "param2", ".")
        ), strings);
    }
}
