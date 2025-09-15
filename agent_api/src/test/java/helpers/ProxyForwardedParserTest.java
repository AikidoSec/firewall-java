package helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.util.HashMap;
import java.util.List;

import static dev.aikido.agent_api.helpers.net.ProxyForwardedParser.getIpFromRequest;
import static org.junit.jupiter.api.Assertions.*;

class ProxyForwardedParserTest {
    private HashMap<String, List<String>> headers;

    @BeforeEach
    void setUp() {
        headers = new HashMap<>();
    }

    @Test
    void testGetIpFromRequest_ValidXForwardedFor() {
        headers.put("X-Forwarded-For", List.of("192.168.1.1, 203.0.113.5"));
        String result = getIpFromRequest("10.0.0.1", headers);
        assertEquals("192.168.1.1", result);
    }

    @Test
    void testGetIpFromRequest_ValidXForwardedForWithPort() {
        headers.put("X-Forwarded-For", List.of("192.168.1.1:8080, 203.0.113.5:443"));
        String result = getIpFromRequest("10.0.0.1", headers);
        assertEquals("192.168.1.1", result);
    }

    @Test
    void testGetIpFromRequest_ValidXForwardedForWithIPv6() {
        headers.put("X-Forwarded-For", List.of("2001:db8::1, 203.0.113.5"));
        String result = getIpFromRequest("10.0.0.1", headers);
        assertEquals("2001:db8::1", result);
    }

    @Test
    void testGetIpFromRequest_ValidXForwardedForWithIPv6AndBrackets() {
        headers.put("X-Forwarded-For", List.of("[2001:db8::1], 203.0.113.5"));
        String result = getIpFromRequest("10.0.0.1", headers);
        assertEquals("2001:db8::1", result);
    }

    @Test
    void testGetIpFromRequest_InvalidXForwardedFor() {
        headers.put("X-Forwarded-For", List.of("invalid.ip.address, 203.0.113.5"));
        String result = getIpFromRequest("10.0.0.1", headers);
        assertEquals("203.0.113.5", result);
    }

    @Test
    void testGetIpFromRequest_InvalidXForwardedFor_with_port() {
        headers.put("X-Forwarded-For", List.of("invalid.ip.address:443, 203.0.113.5:8080192"));
        String result = getIpFromRequest("10.0.0.1", headers);
        assertEquals("203.0.113.5", result);
    }

    @Test
    void testGetIpFromRequest_EmptyXForwardedFor() {
        headers.put("X-Forwarded-For", List.of(""));
        String result = getIpFromRequest("10.0.0.1", headers);
        assertEquals("10.0.0.1", result);
    }

    @Test
    void testGetIpFromRequest_NullXForwardedFor() {
        String result = getIpFromRequest("10.0.0.1", new HashMap<>());
        assertEquals("10.0.0.1", result);
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TRUST_PROXY", value = "0")
    void testGetIpFromRequest_TrustProxyFalse() {
        headers.put("X-Forwarded-For", List.of("192.168.1.1, 203.0.113.5"));
        String result = getIpFromRequest("10.0.0.1", headers);
        assertEquals("10.0.0.1", result);
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TRUST_PROXY", value = "1")
    void testGetIpFromRequest_TrustProxyTrueWithNoValidIPs() {
        headers.put("X-Forwarded-For", List.of("invalid.ip.address, another.invalid"));
        String result = getIpFromRequest("10.0.0.1", headers);
        assertEquals("10.0.0.1", result);
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TRUST_PROXY", value = "1")
    void testGetIpFromRequest_MultipleValidIPs() {
        headers.put("X-Forwarded-For", List.of("203.0.113.5, 192.168.1.1"));
        String result = getIpFromRequest("10.0.0.1", headers);
        assertEquals("203.0.113.5", result);
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TRUST_PROXY", value = "0")
    void testGetIpFromRequest_DefaultHeader() {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("X-Forwarded-For", List.of("127.0.0.1, 192.168.0.1"));
        String result = getIpFromRequest("1.2.3.4", headers);
        assertEquals("1.2.3.4", result);
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_CLIENT_IP_HEADER", value = "connecting-ip")
    void testGetIpFromRequest_CustomHeader() {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("X-Forwarded-For", List.of("127.0.0.1, 192.168.0.1"));
        headers.put("connecting-ip", List.of("9.9.9.9"));
        String result = getIpFromRequest("1.2.3.4", headers);
        assertEquals("9.9.9.9", result);
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_CLIENT_IP_HEADER", value = "connecting-ip")
    @SetEnvironmentVariable(key = "AIKIDO_TRUST_PROXY", value = "0")
    void testGetIpFromRequest_NoCustomHeader() {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("connecting-ip", List.of("5.6.7.8, 192.168.0.1"));
        String result = getIpFromRequest("1.2.3.4", headers);
        assertEquals("1.2.3.4", result);
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_CLIENT_IP_HEADER", value = "connecting-IP")
    void testGetIpFromRequest_CustomHeaderCaseInsensitive() {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("X-Forwarded-For", List.of("127.0.0.1, 192.168.0.1"));
        headers.put("connecting-ip", List.of("9.9.9.9"));
        String result = getIpFromRequest("1.2.3.4", headers);
        assertEquals("9.9.9.9", result);
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_CLIENT_IP_HEADER", value = "")
    void testGetIpFromRequest_EmptyCustomHeader() {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("connecting-ip", List.of("9.9.9.9"));
        String result = getIpFromRequest("1.2.3.4", headers);
        assertEquals("1.2.3.4", result);
    }

    @Test
    void testGetIpFromRequest_MultipleValidIPsInXForwardedFor() {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("X-Forwarded-For", List.of(",5.6.7.8,,"));
        String result = getIpFromRequest("1.2.3.4", headers);
        assertEquals("5.6.7.8", result);
    }
}
