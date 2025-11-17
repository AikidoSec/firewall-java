package vulnerabilities;

import dev.aikido.agent_api.context.User;
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
        String source = "User Input";
        String pathToPayload = "/api/vulnerable";
        Map<String, String> metadata = new HashMap<>();
        metadata.put("userId", "123");
        String payload = "SELECT * FROM users WHERE id = 1";
        String stack = "Stack trace here";
        User user = new User("id", "name", "1.1.1.1", 0);

        // Act
        Attack attack = new Attack(operation, Vulnerabilities.SQL_INJECTION, source, pathToPayload, metadata, payload, stack, user);

        // Assert
        assertEquals(operation, attack.operation);
        assertEquals("sql_injection", attack.kind);
        assertEquals(source, attack.source);
        assertEquals(pathToPayload, attack.pathToPayload);
        assertEquals(metadata, attack.metadata);
        assertEquals(payload, attack.payload);
        assertEquals(stack, attack.stack);
        assertEquals(user, attack.user);
        assertEquals(
                "{\"operation\":\"SQL Injection\",\"kind\":\"sql_injection\",\"source\":\"User Input\",\"pathToPayload\":\"/api/vulnerable\",\"metadata\":{\"userId\":\"123\"},\"payload\":\"SELECT * FROM users WHERE id \\u003d 1\",\"stack\":\"Stack trace here\",\"user\":{\"id\":\"id\",\"name\":\"name\",\"lastIpAddress\":\"1.1.1.1\",\"firstSeenAt\":0,\"lastSeenAt\":0}}",
                attack.toString()
        );
    }

    @Test
    public void testAttackWithEmptyMetadata() {
        // Arrange
        String operation = "XSS Attack";
        String source = "User Input";
        String pathToPayload = "/api/vulnerable";
        Map<String, String> metadata = new HashMap<>(); // Empty metadata
        String payload = "<script>alert('XSS');</script>";
        String stack = "Stack trace here";
        User user = new User("123", "name", "1.1.1.1", 0);

        // Act
        Attack attack = new Attack(operation, Vulnerabilities.SQL_INJECTION, source, pathToPayload, metadata, payload, stack, user);

        // Assert
        assertEquals(operation, attack.operation);
        assertEquals("sql_injection", attack.kind);
        assertEquals(source, attack.source);
        assertEquals(pathToPayload, attack.pathToPayload);
        assertEquals(metadata, attack.metadata);
        assertEquals(payload, attack.payload);
        assertEquals(stack, attack.stack);
        assertEquals(
            "{\"operation\":\"XSS Attack\",\"kind\":\"sql_injection\",\"source\":\"User Input\",\"pathToPayload\":\"/api/vulnerable\",\"metadata\":{},\"payload\":\"\\u003cscript\\u003ealert(\\u0027XSS\\u0027);\\u003c/script\\u003e\",\"stack\":\"Stack trace here\",\"user\":{\"id\":\"123\",\"name\":\"name\",\"lastIpAddress\":\"1.1.1.1\",\"firstSeenAt\":0,\"lastSeenAt\":0}}",
                attack.toString()
        );
    }
}
