package context;

import static org.junit.jupiter.api.Assertions.*;

import dev.aikido.agent_api.context.SpringMVCContextObject;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

class SpringMVCContextObjectTest {

    private SpringMVCContextObject springContextObject;

    @BeforeEach
    void setUp() {
        springContextObject = new SpringMVCContextObject(
                "GET",
                new StringBuffer("http://localhost/test"),
                "192.168.1.1",
                Map.of(),
                new HashMap<>(),
                new HashMap<>());
    }

    @Test
    void testGetRouteWithSlashTest() {
        // Act
        springContextObject = new SpringMVCContextObject(
                "GET",
                new StringBuffer("http://localhost/test"),
                "192.168.1.1",
                Map.of(),
                new HashMap<>(),
                new HashMap<>());

        // Assert
        assertEquals("http://localhost/test", springContextObject.getUrl());
        assertEquals("/test", springContextObject.getRoute());
    }

    @Test
    void testGetRouteWithNumbers() {
        // Act
        springContextObject = new SpringMVCContextObject(
                "GET",
                new StringBuffer("http://localhost/api/dog/28632"),
                "192.168.1.1",
                Map.of(),
                new HashMap<>(),
                new HashMap<>());

        // Assert
        assertEquals("http://localhost/api/dog/28632", springContextObject.getUrl());
        assertEquals("/api/dog/:number", springContextObject.getRoute());
    }

    @Test
    void testSetParams() {
        springContextObject.setParameter("123", "345");
        assertEquals("345", springContextObject.getParams().get("123"));
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TRUST_PROXY", value = "1")
    void testIpRequestFeature() {
        HashMap<String, String> headers = new HashMap<>(Map.of("x-forwarded-for", "invalid.ip, in.va.li.d, 1.2.3.4"));
        springContextObject = new SpringMVCContextObject(
                "GET",
                new StringBuffer("http://localhost/api/dog/28632"),
                "192.168.1.1",
                Map.of(),
                new HashMap<>(),
                headers);
        assertEquals(1, springContextObject.getHeaders().size());
        assertEquals("1.2.3.4", springContextObject.getRemoteAddress());
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TRUST_PROXY", value = "1")
    void testIpRequestFeature_InvalidHeader() {
        HashMap<String, String> headers = new HashMap<>(Map.of("x-forwarded-for", "invalid.ip, in.va.li.d"));
        springContextObject = new SpringMVCContextObject(
                "GET",
                new StringBuffer("http://localhost/api/dog/28632"),
                "192.168.1.1",
                Map.of(),
                new HashMap<>(),
                headers);
        assertEquals(1, springContextObject.getHeaders().size());
        assertEquals("192.168.1.1", springContextObject.getRemoteAddress());
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TRUST_PROXY", value = "0")
    void testIpRequestFeature_TrustProxyOff() {
        HashMap<String, String> headers = new HashMap<>(Map.of("x-forwarded-for", "invalid.ip, in.va.li.d, 1.2.3.4"));
        springContextObject = new SpringMVCContextObject(
                "GET",
                new StringBuffer("http://localhost/api/dog/28632"),
                "192.168.1.1",
                Map.of(),
                new HashMap<>(),
                headers);
        assertEquals(1, springContextObject.getHeaders().size());
        assertEquals("192.168.1.1", springContextObject.getRemoteAddress());
    }
}
