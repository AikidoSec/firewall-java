package background.ipc_commands;


import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.background.cloud.api.events.DetectedAttack;
import dev.aikido.agent_api.background.ipc_commands.AttackCommand;
import dev.aikido.agent_api.background.ipc_commands.Command;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.helpers.env.Token;
import dev.aikido.agent_api.vulnerabilities.Attack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.EmptySampleContextObject;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class AttackCommandTest {
    private BlockingQueue<APIEvent> queue;
    private CloudConnectionManager connectionManager;
    private AttackCommand attackCommand;

    @BeforeEach
    void setUp() {
        queue = new LinkedBlockingQueue<APIEvent>();
        connectionManager = new CloudConnectionManager(true, new Token("xyz"), null, null);
        attackCommand = new AttackCommand(queue);
    }

    @Test
    void testExecuteWithValidData() {
        // Arrange
        Attack attack = mock(Attack.class);
        ContextObject context = new EmptySampleContextObject();
        AttackCommand.Req commandData = new AttackCommand.Req(attack, context);

        // Act
        assertEquals(0, connectionManager.getStats().getAttacksBlocked());
        assertEquals(0, connectionManager.getStats().getAttacksDetected());
        Optional<Command.EmptyResult> result = attackCommand.execute(commandData, connectionManager);

        // Assert
        assertTrue(result.isEmpty());
        assertEquals(1, queue.size());
        assertEquals(1, connectionManager.getStats().getAttacksBlocked());
        assertEquals(1, connectionManager.getStats().getAttacksDetected());

        // Capture the added event
        assertTrue(queue.poll() instanceof DetectedAttack.DetectedAttackEvent);

        // Now increment count :
        attackCommand.execute(commandData, connectionManager);
        assertEquals(1, queue.size());
        assertEquals(2, connectionManager.getStats().getAttacksBlocked());
        assertEquals(2, connectionManager.getStats().getAttacksDetected());

        // Now increment count [blocking disabled] :
        connectionManager = new CloudConnectionManager(false, new Token("xyz"), null, null);
        attackCommand.execute(commandData, connectionManager);
        assertEquals(2, queue.size());
        assertEquals(0, connectionManager.getStats().getAttacksBlocked());
        assertEquals(1, connectionManager.getStats().getAttacksDetected());
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
