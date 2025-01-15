package background.ipc_commands;


import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.background.cloud.api.events.DetectedAttack;
import dev.aikido.agent_api.background.ipc_commands.AttackCommand;
import dev.aikido.agent_api.background.ipc_commands.Command;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.vulnerabilities.Attack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import utils.EmptySampleContextObject;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AttackCommandTest {
    private BlockingQueue<APIEvent> queue;
    private CloudConnectionManager connectionManager;
    private AttackCommand attackCommand;

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
        ContextObject context = new EmptySampleContextObject();
        AttackCommand.Req commandData = new AttackCommand.Req(attack, context);

        // Act
        Optional<Command.EmptyResult> result = attackCommand.execute(commandData, connectionManager);

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
        AttackCommand.Req commandData = new AttackCommand.Req(null, context);

        // Act
        Optional<?> result = attackCommand.execute(commandData, connectionManager);

        // Assert
        assertTrue(result.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    void testExecuteWithMissingContext() {
        // Arrange
        Attack attack = mock(Attack.class);
        AttackCommand.Req commandData = new AttackCommand.Req(attack, null);

        // Act
        Optional<?> result = attackCommand.execute(commandData, connectionManager);

        // Assert
        assertTrue(result.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    void testThatInputOutputClassIsCorrect() {
        assertEquals(Command.EmptyResult.class, new AttackCommand(null).getOutputClass());
        assertEquals(AttackCommand.Req.class, new AttackCommand(null).getInputClass());
    }
}