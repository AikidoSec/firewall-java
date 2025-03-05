package helpers;

import dev.aikido.agent_api.helpers.env.BooleanEnv;
import dev.aikido.agent_api.helpers.net.ProxyForwardedParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.util.HashMap;
import java.util.List;
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
        String result = parser.getIpFromRequest("10.0.0.1", "192.168.1.1, 203.0.113.5");
        assertEquals("192.168.1.1", result);
    }

    @Test
    void testGetIpFromRequest_ValidXForwardedForWithPort() {
        String result = parser.getIpFromRequest("10.0.0.1", "192.168.1.1:8080, 203.0.113.5:443");
        assertEquals("192.168.1.1", result);
    }

    @Test
    void testGetIpFromRequest_ValidXForwardedForWithIPv6() {
        String result = parser.getIpFromRequest("10.0.0.1", "2001:db8::1, 203.0.113.5");
        assertEquals("2001:db8::1", result);
    }

    @Test
    void testGetIpFromRequest_InvalidXForwardedFor() {
        String result = parser.getIpFromRequest("10.0.0.1", "invalid.ip.address, 203.0.113.5");
        assertEquals("203.0.113.5", result); // Only use 203.0.113.5
    }

    @Test
    void testGetIpFromRequest_InvalidXForwardedFor_with_port() {
        String result = parser.getIpFromRequest("10.0.0.1", "invalid.ip.address:443, 203.0.113.5:8080192");
        assertEquals("203.0.113.5", result); // Only use 203.0.113.5
    }

    @Test
    void testGetIpFromRequest_EmptyXForwardedFor() {
        String result = parser.getIpFromRequest("10.0.0.1", "");
        assertEquals("10.0.0.1", result); // Fallback to raw IP
    }

    @Test
    void testGetIpFromRequest_NullXForwardedFor() {
        String result = parser.getIpFromRequest("10.0.0.1", null);
        assertEquals("10.0.0.1", result); // Fallback to raw IP
    }

    @Test
    @SetEnvironmentVariable(key="AIKIDO_TRUST_PROXY", value = "0")
    void testGetIpFromRequest_TrustProxyFalse() {
        String result = parser.getIpFromRequest("10.0.0.1", "192.168.1.1, 203.0.113.5");
        assertEquals("10.0.0.1", result); // Should fallback to raw IP
    }

    @Test
    @SetEnvironmentVariable(key="AIKIDO_TRUST_PROXY", value = "1")
    void testGetIpFromRequest_TrustProxyTrueWithNoValidIPs() {
        // Mocking the BooleanEnv to return true
        BooleanEnv mockTrustProxy = new BooleanEnv("AIKIDO_TRUST_PROXY", true);
        String result = parser.getIpFromRequest("10.0.0.1", "invalid.ip.address, another.invalid");
        assertEquals("10.0.0.1", result); // Should fallback to raw IP
    }

    @Test
    @SetEnvironmentVariable(key="AIKIDO_TRUST_PROXY", value = "1")
    void testGetIpFromRequest_MultipleValidIPs() {
        String result = parser.getIpFromRequest("10.0.0.1", "203.0.113.5, 192.168.1.1");
        assertEquals("203.0.113.5", result); // Should return the first valid IP
    }

}