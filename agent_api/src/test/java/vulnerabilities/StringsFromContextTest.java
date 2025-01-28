package vulnerabilities;

import dev.aikido.agent_api.context.SpringContextObject;
import dev.aikido.agent_api.vulnerabilities.StringsFromContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringsFromContextTest {
    private SpringContextObject springContextObject;

    @BeforeEach
    void setUp() {
        springContextObject = new SpringContextObject(
                "GET", new StringBuffer("http://localhost/test"), "192.168.1.1",
                /* query: */ Map.of("param1", new String[]{"value1"}, "param2",  new String[]{"value2"}),
                /* cookies: */ new HashMap<>(Map.of(
                        "sessionId", List.of("abc123"),
                        "userId", List.of("user1"))),
                /* headers: */ new HashMap<>(Map.of(
                        "content-type", "application/json",
                        "authorization", "Bearer token"))
        );
    }

    @Test
    public void testExtractsFromRequest() {
        Map<String, Map<String, String>> strings = new StringsFromContext(springContextObject).getAll();
        assertEquals(Map.of(
                "body", Map.of(),
                "headers", Map.of(
                        "content-type", ".",
                        "authorization", ".",
                        "application/json", ".content-type",
                        "Bearer token", ".authorization"),
                "cookies", Map.of(
                        "user1", ".userId.[0]",
                        "abc123", ".sessionId.[0]",
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
        springContextObject.setBody(List.of("1", "20", "2"));
        Map<String, Map<String, String>> strings = new StringsFromContext(springContextObject).getAll();
        assertEquals(Map.of(
                "body", Map.of("1", ".[0]", "2", ".[2]", "20",".[1]"),
                "headers", Map.of(
                        "content-type", ".",
                        "authorization", ".",
                        "application/json", ".content-type",
                        "Bearer token", ".authorization"),
                "cookies", Map.of(
                        "user1", ".userId.[0]",
                        "abc123", ".sessionId.[0]",
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
        springContextObject.setParams(List.of("1", "20", "2"));
        Map<String, Map<String, String>> strings = new StringsFromContext(springContextObject).getAll();
        assertEquals(Map.of(
                "body", Map.of(),
                "headers", Map.of(
                        "content-type", ".",
                        "authorization", ".",
                        "application/json", ".content-type",
                        "Bearer token", ".authorization"),
                "cookies", Map.of(
                        "user1", ".userId.[0]",
                        "abc123", ".sessionId.[0]",
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
