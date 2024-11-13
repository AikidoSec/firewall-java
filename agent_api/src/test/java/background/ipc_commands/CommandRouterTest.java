package background.ipc_commands;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.ipc_commands.CommandRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class CommandRouterTest {
    private CommandRouter commandRouter;
    private CloudConnectionManager cloudConnectionManager;

    @BeforeEach
    public void setup() {
        cloudConnectionManager = Mockito.mock(CloudConnectionManager.class);
        commandRouter = new CommandRouter(
                cloudConnectionManager,
                new LinkedBlockingQueue<>()
        );
    }
    @Test
    public void testIPCInputIsMalformed() {
        Optional<String> result = commandRouter.parseIPCInput("BLOCKING_ENABLED%{}");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testNonExistingCommand() {
        Optional<String> result = commandRouter.parseIPCInput("INVALID_COMMAND${}");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testShouldBlockCommand() {
        when(cloudConnectionManager.shouldBlock()).thenReturn(true);
        Optional<String> result = commandRouter.parseIPCInput("BLOCKING_ENABLED${}");
        assertTrue(result.isPresent());
        assertEquals("{\"isBlockingEnabled\":true}", result.get());

        when(cloudConnectionManager.shouldBlock()).thenReturn(false);
        Optional<String> result2 = commandRouter.parseIPCInput("BLOCKING_ENABLED${}");
        assertTrue(result2.isPresent());
        assertEquals("{\"isBlockingEnabled\":false}", result2.get());
    }
}
