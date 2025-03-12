package background.ipc_commands;

import com.google.gson.Gson;
import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.ipc_commands.BlockingEnabledCommand;
import dev.aikido.agent_api.background.ipc_commands.CommandRouter;
import dev.aikido.agent_api.storage.ConfigStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void testShouldBlockCommand() throws IOException {
        byte[] blockingTrue = new Gson()
                .toJson(new BlockingEnabledCommand.Res(true))
                .getBytes(StandardCharsets.UTF_8);
        byte[] blockingFalse = new Gson()
                .toJson(new BlockingEnabledCommand.Res(false))
                .getBytes(StandardCharsets.UTF_8);

        ConfigStore.updateBlocking(true);
        Optional<byte[]> result = commandRouter.parseIPCInput("BLOCKING_ENABLED${}".getBytes(StandardCharsets.UTF_8));

        assertTrue(result.isPresent());
        assertArrayEquals(blockingTrue, result.get());


        ConfigStore.updateBlocking(false);
        Optional<byte[]> result2 = commandRouter.parseIPCInput("BLOCKING_ENABLED${}".getBytes(StandardCharsets.UTF_8));

        assertTrue(result2.isPresent());
        assertArrayEquals(blockingFalse, result2.get());
    }
}
