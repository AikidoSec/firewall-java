package vulnerabilities;

import dev.aikido.agent_api.vulnerabilities.Attack;
import dev.aikido.agent_api.vulnerabilities.Vulnerabilities;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AttackTest {

    @Test
    public void testAttackConstructor() {
        // Arrange
        String operation = "SQL Injection";
        Vulnerabilities.Vulnerability vulnerability = new Vulnerabilities.SQLInjectionVulnerability();
        String source = "User Input";
        String pathToPayload = "/api/vulnerable";
        Map<String, String> metadata = new HashMap<>();
        metadata.put("userId", "123");
        String payload = "SELECT * FROM users WHERE id = 1";
        String stack = "Stack trace here";

        // Act
        Attack attack = new Attack(operation, vulnerability, source, pathToPayload, metadata, payload, stack);

        // Assert
        assertEquals(operation, attack.operation);
        assertEquals(vulnerability.getKind(), attack.kind);
        assertEquals(source, attack.source);
        assertEquals(pathToPayload, attack.pathToPayload);
        assertEquals(metadata, attack.metadata);
        assertEquals(payload, attack.payload);
        assertEquals(stack, attack.stack);
    }

    @Test
    public void testAttackWithEmptyMetadata() {
        // Arrange
        String operation = "XSS Attack";
        Vulnerabilities.Vulnerability vulnerability = new Vulnerabilities.SQLInjectionVulnerability();
        String source = "User Input";
        String pathToPayload = "/api/vulnerable";
        Map<String, String> metadata = new HashMap<>(); // Empty metadata
        String payload = "<script>alert('XSS');</script>";
        String stack = "Stack trace here";

        // Act
        Attack attack = new Attack(operation, vulnerability, source, pathToPayload, metadata, payload, stack);

        // Assert
        assertEquals(operation, attack.operation);
        assertEquals(vulnerability.getKind(), attack.kind);
        assertEquals(source, attack.source);
        assertEquals(pathToPayload, attack.pathToPayload);
        assertEquals(metadata, attack.metadata);
        assertEquals(payload, attack.payload);
        assertEquals(stack, attack.stack);
    }
}
