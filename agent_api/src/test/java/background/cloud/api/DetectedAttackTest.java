package background.cloud.api;

import dev.aikido.agent_api.background.cloud.api.events.DetectedAttack;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.vulnerabilities.Attack;
import dev.aikido.agent_api.vulnerabilities.Vulnerabilities;
import org.junit.jupiter.api.Test;
import utils.EmptySampleContextObject;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DetectedAttackTest {

    @Test
    void createAPIEvent_WithValidContextAndAttack_ReturnsDetectedAttackEvent() {
        // Arrange
        ContextObject context = new EmptySampleContextObject("test", "/api/resource", "POST");

        Map<String, String> metadata = new HashMap<>();
        metadata.put("key", "value");

        Attack attack = new Attack(
            "read",
            new Vulnerabilities.SQLInjectionVulnerability(),
            "web",
            "/api/resource",
            metadata,
            "test_payload",
            "stack_trace",
            null
        );

        // Act
        DetectedAttack.DetectedAttackEvent event = DetectedAttack.createAPIEvent(attack, context);

        // Assert
        assertNotNull(event);
        assertEquals("detected_attack", event.type());
        assertNotNull(event.request());
        assertEquals("POST", event.request().method());
        assertEquals("web", event.request().source());
        assertEquals("/api/resource", event.request().route());
        assertNotNull(event.attack());
        assertEquals("sql_injection", event.attack().kind());
        assertEquals("read", event.attack().operation());
        assertEquals("web", event.attack().source());
        assertEquals("/api/resource", event.attack().path());
        assertEquals("test_payload", event.attack().payload());
        assertEquals(metadata, event.attack().metadata());
        assertEquals("module", event.attack().module());
        assertEquals("stack_trace", event.attack().stack());
        assertNull(event.attack().user());
        assertNotNull(event.agent());
        assertTrue(event.time() > 0);
    }

    @Test
    void createAPIEvent_WithNullContext_ReturnsDetectedAttackEventWithNullRequest() {
        // Arrange
        Attack attack = new Attack(
            "read",
            new Vulnerabilities.SQLInjectionVulnerability(),
            "web",
            "/api/resource",
            Map.of("key", "value"),
            "test_payload",
            "stack_trace",
            null
        );

        // Act
        DetectedAttack.DetectedAttackEvent event = DetectedAttack.createAPIEvent(attack, null);

        // Assert
        assertNotNull(event);
        assertEquals("detected_attack", event.type());
        assertNull(event.request());
        assertNotNull(event.attack());
        assertEquals("sql_injection", event.attack().kind());
        assertEquals("read", event.attack().operation());
        assertEquals("web", event.attack().source());
        assertEquals("/api/resource", event.attack().path());
        assertEquals("test_payload", event.attack().payload());
        assertEquals(Map.of("key", "value"), event.attack().metadata());
        assertEquals("module", event.attack().module());
        assertEquals("stack_trace", event.attack().stack());
        assertNull(event.attack().user());
        assertNotNull(event.agent());
        assertTrue(event.time() > 0);
    }
}
