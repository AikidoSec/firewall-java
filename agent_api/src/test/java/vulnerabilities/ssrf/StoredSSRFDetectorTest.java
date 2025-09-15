package vulnerabilities.ssrf;

import dev.aikido.agent_api.vulnerabilities.Attack;
import dev.aikido.agent_api.vulnerabilities.ssrf.StoredSSRFDetector;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class StoredSSRFDetectorTest {

    private final StoredSSRFDetector detector = new StoredSSRFDetector();

    @Test
    void run_WhenHostnameIsNull_ReturnsNull() {
        Attack result = detector.run(null, List.of("169.254.169.254"), "testOperation");
        assertNull(result);
    }

    @Test
    void run_WhenHostnameIsEmpty_ReturnsNull() {
        Attack result = detector.run("", List.of("169.254.169.254"), "testOperation");
        assertNull(result);
    }

    @Test
    void run_WhenHostnameIsTrusted_ReturnsNull() {
        Attack result = detector.run("metadata.google.internal", List.of("169.254.169.254"), "testOperation");
        assertNull(result);
    }

    @Test
    void run_WhenIpIsAwsImdsIp_ReturnsAttack() {
        Attack result = detector.run("test.example.com", List.of("169.254.169.254"), "testOperation");
        assertNotNull(result);
        assertEquals("testOperation", result.operation);
        assertEquals("stored-ssrf", result.kind);
        assertEquals("test.example.com", result.payload);
        assertEquals("test.example.com", result.metadata.get("hostname"));
        assertEquals("169.254.169.254", result.metadata.get("privateIP"));
        assertNull(result.source);
        assertEquals("", result.pathToPayload);
        assertNull(result.user);
    }

    @Test
    void run_WhenIpIsAlibabaImdsIp_ReturnsAttack() {
        Attack result = detector.run("test.example.com", List.of("100.100.100.200"), "testOperation");
        assertNotNull(result);
        assertEquals("testOperation", result.operation);
        assertEquals("stored-ssrf", result.kind);
        assertEquals("test.example.com", result.payload);
        assertEquals("test.example.com", result.metadata.get("hostname"));
        assertEquals("100.100.100.200", result.metadata.get("privateIP"));
        assertNull(result.source);
        assertEquals("", result.pathToPayload);
        assertNull(result.user);
    }

    @Test
    void run_WhenIpIsIpv6ImdsIp_ReturnsAttack() {
        Attack result = detector.run("test.example.com", List.of("fd00:ec2::254"), "testOperation");
        assertNotNull(result);
        assertEquals("testOperation", result.operation);
        assertEquals("stored-ssrf", result.kind);
        assertEquals("test.example.com", result.payload);
        assertEquals("test.example.com", result.metadata.get("hostname"));
        assertEquals("fd00:ec2::254", result.metadata.get("privateIP"));
        assertNull(result.source);
        assertEquals("", result.pathToPayload);
        assertNull(result.user);
    }

    @Test
    void run_WhenIpIsNotImdsIp_ReturnsNull() {
        Attack result = detector.run("test.example.com", List.of("192.168.1.1"), "testOperation");
        assertNull(result);
    }

    @Test
    void run_WhenMultipleIpsButNoneImds_ReturnsNull() {
        Attack result = detector.run("test.example.com", List.of("192.168.1.1", "10.0.0.1"), "testOperation");
        assertNull(result);
    }

    @Test
    void run_WhenMultipleIpsAndOneImds_ReturnsAttack() {
        Attack result = detector.run("test.example.com", List.of("192.168.1.1", "169.254.169.254"), "testOperation");
        assertNotNull(result);
        assertEquals("169.254.169.254", result.metadata.get("privateIP"));
    }
}
