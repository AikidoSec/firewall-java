package helpers;

import dev.aikido.agent_api.helpers.env.BooleanEnv;
import dev.aikido.agent_api.helpers.net.ProxyForwardedParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProxyForwardedParserTest {

    private ProxyForwardedParser parser;

    @BeforeEach
    void setUp() {
        parser = new ProxyForwardedParser();
    }

    @Test
    void testGetIpFromRequest_ValidXForwardedFor() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-forwarded-for", "192.168.1.1, 203.0.113.5");

        String result = parser.getIpFromRequest("10.0.0.1", headers);
        assertEquals("192.168.1.1", result);
    }

    @Test
    void testGetIpFromRequest_ValidXForwardedForWithPort() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-forwarded-for", "192.168.1.1:8080, 203.0.113.5:443");

        String result = parser.getIpFromRequest("10.0.0.1", headers);
        assertEquals("192.168.1.1", result);
    }

    @Test
    void testGetIpFromRequest_ValidXForwardedForWithIPv6() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-forwarded-for", "2001:db8::1, 203.0.113.5");

        String result = parser.getIpFromRequest("10.0.0.1", headers);
        assertEquals("2001:db8::1", result);
    }

    @Test
    void testGetIpFromRequest_InvalidXForwardedFor() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-forwarded-for", "invalid.ip.address, 203.0.113.5");

        String result = parser.getIpFromRequest("10.0.0.1", headers);
        assertEquals("203.0.113.5", result); // Only use 203.0.113.5
    }

    @Test
    void testGetIpFromRequest_InvalidXForwardedFor_with_port() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-forwarded-for", "invalid.ip.address:443, 203.0.113.5:8080192");

        String result = parser.getIpFromRequest("10.0.0.1", headers);
        assertEquals("203.0.113.5", result); // Only use 203.0.113.5
    }

    @Test
    void testGetIpFromRequest_EmptyXForwardedFor() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-forwarded-for", "");

        String result = parser.getIpFromRequest("10.0.0.1", headers);
        assertEquals("10.0.0.1", result); // Fallback to raw IP
    }

    @Test
    void testGetIpFromRequest_NullXForwardedFor() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-forwarded-for", null);

        String result = parser.getIpFromRequest("10.0.0.1", headers);
        assertEquals("10.0.0.1", result); // Fallback to raw IP
    }

    @Test
    @SetEnvironmentVariable(key="AIKIDO_TRUST_PROXY", value = "0")
    void testGetIpFromRequest_TrustProxyFalse() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-forwarded-for", "192.168.1.1, 203.0.113.5");

        String result = parser.getIpFromRequest("10.0.0.1", headers);
        assertEquals("10.0.0.1", result); // Should fallback to raw IP
    }

    @Test
    @SetEnvironmentVariable(key="AIKIDO_TRUST_PROXY", value = "1")
    void testGetIpFromRequest_TrustProxyTrueWithNoValidIPs() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-forwarded-for", "invalid.ip.address, another.invalid");

        // Mocking the BooleanEnv to return true
        BooleanEnv mockTrustProxy = new BooleanEnv("AIKIDO_TRUST_PROXY", true);
        String result = parser.getIpFromRequest("10.0.0.1", headers);
        assertEquals("10.0.0.1", result); // Should fallback to raw IP
    }

    @Test
    @SetEnvironmentVariable(key="AIKIDO_TRUST_PROXY", value = "1")
    void testGetIpFromRequest_MultipleValidIPs() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-forwarded-for", "203.0.113.5, 192.168.1.1");

        String result = parser.getIpFromRequest("10.0.0.1", headers);
        assertEquals("203.0.113.5", result); // Should return the first valid IP
    }

}
