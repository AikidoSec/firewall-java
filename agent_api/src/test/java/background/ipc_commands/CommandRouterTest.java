package background.ipc_commands;

import com.google.gson.Gson;
import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.ipc_commands.BlockingEnabledCommand;
import dev.aikido.agent_api.background.ipc_commands.CommandRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;
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
        Optional<byte[]> result = commandRouter.parseIPCInput("BLOCKING_ENABLED%{}".getBytes(StandardCharsets.UTF_8));
        assertTrue(result.isEmpty());
    }

    @Test
    public void testNonExistingCommand() {
        Optional<byte[]> result = commandRouter.parseIPCInput("INVALID_COMMAND${}".getBytes(StandardCharsets.UTF_8));
        assertTrue(result.isEmpty());
    }
}
