package context;

import dev.aikido.agent_api.helpers.net.TrustProxy;
import org.junit.jupiter.api.AfterEach;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.JavalinContextObject;
import dev.aikido.agent_api.context.SpringMVCContextObject;
import dev.aikido.agent_api.context.SpringWebfluxContextObject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.InetSocketAddress;

import static dev.aikido.agent_api.vulnerabilities.ssrf.RequestToItselfChecker.isRequestToItself;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SpringMVCContextObjectTest {

    private SpringMVCContextObject springContextObject;

    @BeforeEach
    void setUp() {
        TrustProxy.clearCache();
        springContextObject = new SpringMVCContextObject(
                "GET", new StringBuffer("http://localhost/test"), "192.168.1.1", Map.of(), new HashMap<>(), new HashMap<>(), null
        );
    }

    @AfterEach
    void clearProxyCache() {
        TrustProxy.clearCache();
    }

    @ParameterizedTest
    @SetEnvironmentVariable(key = "AIKIDO_TRUST_PROXY", value = "127.0.0.1/32")
    @CsvSource({"127.0.0.1, true, 1.2.3.4", "8.8.8.8, false, 8.8.8.8"})
    void proxyTrustUsesOriginalPeerInEveryContext(String peer, boolean trusted, String clientIp) {
        HashMap<String, Enumeration<String>> mvcHeaders = new HashMap<>();
        mvcHeaders.put("X-Forwarded-For", Collections.enumeration(List.of("9.9.9.9, 1.2.3.4")));
        List<ContextObject> contexts = List.of(
                new SpringMVCContextObject("GET", new StringBuffer("http://localhost/test"), peer,
                        Map.of(), new HashMap<>(), mvcHeaders, null),
                new JavalinContextObject("GET", "http://localhost/test", peer,
                        Map.of(), new HashMap<>(), Map.of("X-Forwarded-For", "9.9.9.9, 1.2.3.4")),
                new SpringWebfluxContextObject("GET", "http://localhost/test", new InetSocketAddress(peer, 1234),
                        new HashMap<>(), Map.of(), Map.of("X-Forwarded-For", List.of("9.9.9.9, 1.2.3.4")))
        );
        for (ContextObject context : contexts) {
            assertEquals(clientIp, context.getRemoteAddress());
            assertEquals(trusted, context.isBehindTrustedProxy());
            assertEquals(trusted, isRequestToItself(context.getUrl(), "localhost", 80, context.isBehindTrustedProxy()));
        }
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
