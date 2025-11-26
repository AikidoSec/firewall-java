package vulnerabilities.ssrf;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.storage.AttackQueue;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import dev.aikido.agent_api.vulnerabilities.Attack;
import dev.aikido.agent_api.vulnerabilities.ssrf.StoredSSRFDetector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.EmptySampleContextObject;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static utils.EmptyAPIResponses.emptyAPIResponse;
import static utils.EmptyAPIResponses.setEmptyConfigWithEndpointList;

class StoredSSRFDetectorTest {

    private final StoredSSRFDetector detector = new StoredSSRFDetector();

    @BeforeEach
    void setUp() throws InterruptedException {
        AttackQueue.clear();
        setEmptyConfigWithEndpointList(List.of(
            new Endpoint(
                /* method */ "*", /* route */ "/api2/*",
                /* rlm params */ 0, 0,
                /* Allowed IPs */ List.of(), /* graphql */ false,
                /* forceProtectionOff */ true, /* rlm */ false
            ),
            new Endpoint(
                /* method */ "*", /* route */ "/api3/*",
                /* rlm params */ 0, 0,
                /* Allowed IPs */ List.of(), /* graphql */ false,
                /* forceProtectionOff */ false, /* rlm */ false
            )
        ));
    }
    @AfterEach
    void cleanup() {
        Context.set(null);
        AttackQueue.clear();
        ServiceConfigStore.updateFromAPIResponse(emptyAPIResponse);
    }

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
        assertEquals("stored_ssrf", result.kind);
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
        assertEquals("stored_ssrf", result.kind);
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
        assertEquals("stored_ssrf", result.kind);
        assertEquals("test.example.com", result.payload);
        assertEquals("test.example.com", result.metadata.get("hostname"));
        assertEquals("fd00:ec2::254", result.metadata.get("privateIP"));
        assertNull(result.source);
        assertEquals("", result.pathToPayload);
        assertNull(result.user);
    }

    @Test
    void run_WhenProtectionForcedOff() {
        // prepare forced off context
        EmptySampleContextObject context1 = new EmptySampleContextObject("", "http://localhost:3000/api2/test/2/4");
        context1.setRoute("/api2/test/2/4");
        Context.set(context1);

        Attack result = detector.run("test.example.com", List.of("fd00:ec2::254"), "testOperation");
        assertNull(result);

        Context.set(new EmptySampleContextObject());
        result = detector.run("test.example.com", List.of("fd00:ec2::254"), "testOperation");
        assertNotNull(result);

        assertEquals("testOperation", result.operation);
        assertEquals("stored_ssrf", result.kind);
        assertEquals("test.example.com", result.payload);
        assertEquals("test.example.com", result.metadata.get("hostname"));
        assertEquals("fd00:ec2::254", result.metadata.get("privateIP"));
        assertNull(result.source);
        assertEquals("", result.pathToPayload);
        assertNull(result.user);
    }

    @Test
    void run_WhenIpIsIpv6ImdsIp_ReturnsAttackNotWhenIpIsHostname() {
        Attack result = detector.run("fd00:ec2::254", List.of("fd00:ec2::254"), "testOperation");
        assertNull(result);
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
