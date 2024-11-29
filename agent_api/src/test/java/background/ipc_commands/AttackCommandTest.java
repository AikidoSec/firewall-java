package background.ipc_commands;


import com.google.gson.Gson;
import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.background.cloud.api.events.DetectedAttack;
import dev.aikido.agent_api.background.ipc_commands.AttackCommand;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.vulnerabilities.Attack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AttackCommandTest {
    private BlockingQueue<APIEvent> queue;
    private CloudConnectionManager connectionManager;
    private AttackCommand attackCommand;

    public static class SampleContextObject extends ContextObject {
        public SampleContextObject() {
            // Directly initializing fields
            this.method = "GET";
            this.source = "web";
            this.url = "https://example.com/api/resource";
            this.route = "/api/resource";
            remoteAddress = "192.168.1.1";

            // Initialize headers
            this.headers = new HashMap<>();
            this.headers.put("Authorization", "Bearer token");
            this.headers.put("Content-Type", "application/json");

            // Initialize query parameters
            this.query = new HashMap<>();
            this.query.put("search", new String[]{"example", "test"});

            // Initialize cookies
            this.cookies = new HashMap<>();
            this.cookies.put("sessionId", "abc123");

            // Set the body
            this.body = "{\"key\":\"value\"}"; // Body as a JSON string
        }
    }
    @BeforeEach
    void setUp() {
        queue = new LinkedBlockingQueue<APIEvent>();
        connectionManager = mock(CloudConnectionManager.class);
        attackCommand = new AttackCommand(queue);
    }

    @Test
    void testExecuteWithValidData() {
        // Arrange
        Attack attack = mock(Attack.class);
        ContextObject context = new SampleContextObject();
        AttackCommand.AttackCommandData commandData = new AttackCommand.AttackCommandData(attack, context);
        Gson gson = new Gson();
        String jsonData = gson.toJson(commandData);

        // Act
        Optional<String> result = attackCommand.execute(jsonData, connectionManager);

        // Assert
        assertTrue(result.isEmpty());
        assertEquals(1, queue.size());

        // Capture the added event
        ArgumentCaptor<DetectedAttack.DetectedAttackEvent> captor = ArgumentCaptor.forClass(DetectedAttack.DetectedAttackEvent.class);
        verify(connectionManager, times(1)).getManagerInfo(); // Ensure connection manager is called
        assertTrue(queue.poll() instanceof DetectedAttack.DetectedAttackEvent);
    }

    @Test
    void testExecuteWithMissingAttack() {
        // Arrange
        ContextObject context = mock(ContextObject.class);
        AttackCommand.AttackCommandData commandData = new AttackCommand.AttackCommandData(null, context);
        Gson gson = new Gson();
        String jsonData = gson.toJson(commandData);

        // Act
        Optional<String> result = attackCommand.execute(jsonData, connectionManager);

        // Assert
        assertTrue(result.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    void testExecuteWithMissingContext() {
        // Arrange
        Attack attack = mock(Attack.class);
        AttackCommand.AttackCommandData commandData = new AttackCommand.AttackCommandData(attack, null);
        Gson gson = new Gson();
        String jsonData = gson.toJson(commandData);

        // Act
        Optional<String> result = attackCommand.execute(jsonData, connectionManager);

        // Assert
        assertTrue(result.isEmpty());
        assertEquals(0, queue.size());
    }
}