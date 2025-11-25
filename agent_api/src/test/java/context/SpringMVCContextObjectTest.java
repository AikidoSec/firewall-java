package context;

import dev.aikido.agent_api.context.SpringMVCContextObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SpringMVCContextObjectTest {

    private SpringMVCContextObject springContextObject;

    @BeforeEach
    void setUp() {
        springContextObject = new SpringMVCContextObject(
                "GET", new StringBuffer("http://localhost/test"), "192.168.1.1", Map.of(), new HashMap<>(), new HashMap<>(), null
        );
    }

    @Test
    void testGetRouteWithSlashTest() {
        // Act
        springContextObject = new SpringMVCContextObject(
                "GET", new StringBuffer("http://localhost/test"), "192.168.1.1", Map.of(), new HashMap<>(), new HashMap<>(), "a=b"
        );

        // Assert
        assertEquals("http://localhost/test?a=b", springContextObject.getUrl());
        assertEquals("/test", springContextObject.getRoute());
    }

    @Test
    void testGetRouteWithNumbers() {
        // Act
        springContextObject = new SpringMVCContextObject(
                "GET", new StringBuffer("http://localhost/api/dog/28632"), "192.168.1.1", Map.of(), new HashMap<>(), new HashMap<>(), ""
        );

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
        // Create headers with Enumeration
        Vector<String> forwardedForValues = new Vector<>(List.of("invalid.ip", "in.va.li.d", "1.2.3.4"));
        HashMap<String, Enumeration<String>> headers = new HashMap<>();
        headers.put("x-forwarded-for", forwardedForValues.elements());

        springContextObject = new SpringMVCContextObject(
                "GET", new StringBuffer("http://localhost/api/dog/28632"), "192.168.1.1", Map.of(), new HashMap<>(), headers, null
        );

        assertEquals(1, springContextObject.getHeaders().size());
        assertEquals("1.2.3.4", springContextObject.getRemoteAddress());
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TRUST_PROXY", value = "1")
    void testIpRequestFeature_InvalidHeader() {
        // Create headers with Enumeration
        Vector<String> forwardedForValues = new Vector<>(List.of("invalid.ip", "in.va.li.d"));
        HashMap<String, Enumeration<String>> headers = new HashMap<>();
        headers.put("x-forwarded-for", forwardedForValues.elements());

        springContextObject = new SpringMVCContextObject(
                "GET", new StringBuffer("http://localhost/api/dog/28632"), "192.168.1.1", Map.of(), new HashMap<>(), headers, null
        );

        assertEquals(1, springContextObject.getHeaders().size());
        assertEquals("192.168.1.1", springContextObject.getRemoteAddress());
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TRUST_PROXY", value = "0")
    void testIpRequestFeature_TrustProxyOff() {
        // Create headers with Enumeration
        Vector<String> forwardedForValues = new Vector<>(List.of("invalid.ip", "in.va.li.d", "1.2.3.4"));
        HashMap<String, Enumeration<String>> headers = new HashMap<>();
        headers.put("x-forwarded-for", forwardedForValues.elements());

        springContextObject = new SpringMVCContextObject(
                "GET", new StringBuffer("http://localhost/api/dog/28632"), "192.168.1.1", Map.of(), new HashMap<>(), headers, null
        );

        assertEquals(1, springContextObject.getHeaders().size());
        assertEquals("192.168.1.1", springContextObject.getRemoteAddress());
    }
}
