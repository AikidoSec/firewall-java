package background.ipc_commands;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.ipc_commands.ShouldRateLimitCommand;
import dev.aikido.agent_api.background.ipc_commands.UpdateAgentDataCommand;
import dev.aikido.agent_api.helpers.env.Token;
import dev.aikido.agent_api.storage.Hostnames;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class UpdateAgentDataCommandTest {
    private UpdateAgentDataCommand command;
    private CloudConnectionManager connectionManager;

    @BeforeEach
    void setUp() {
        command = new UpdateAgentDataCommand();
        connectionManager = new CloudConnectionManager(false, new Token("abc"), null);
    }

    @Test
    void testUpdatesHostnames() {
        connectionManager.getHostnames().add("some.hostname", 4040);
        connectionManager.getStats().incrementTotalHits(5);
        var hostnames = new Hostnames(500);
        hostnames.add("aikido.dev", 443);
        hostnames.add("tile.org", 8080);
        var res = new UpdateAgentDataCommand.Res(null, 0, false, hostnames.asArray());
        command.execute(res, connectionManager);

        // Check:
        var array = connectionManager.getHostnames().asArray();
        assertEquals(3, array.length);
        assertEquals("some.hostname", array[0].getHostname());
        assertEquals(4040, array[0].getPort());
        assertEquals("aikido.dev", array[1].getHostname());
        assertEquals(443, array[1].getPort());
        assertEquals("tile.org", array[2].getHostname());
        assertEquals(8080, array[2].getPort());
        assertEquals(5, connectionManager.getStats().getTotalHits());
        assertEquals(0, connectionManager.getRoutes().size());
    }

    @Test
    void testUpdateTotalHits() {
        connectionManager.getStats().incrementTotalHits(5);
        var res = new UpdateAgentDataCommand.Res(null, 500, false, null);
        command.execute(res, connectionManager);

        // Check:
        var array = connectionManager.getHostnames().asArray();
        assertEquals(0, array.length);
        assertEquals(505, connectionManager.getStats().getTotalHits());
        assertEquals(0, connectionManager.getRoutes().size());
    }

    @Test
    void testNoUpdateIfMiddlewareInstalledIsFalse() {
        connectionManager.getConfig().setMiddlewareInstalled();
        var res = new UpdateAgentDataCommand.Res(null, 500, false, null);
        command.execute(res, connectionManager);

        assertTrue(connectionManager.getConfig().isMiddlewareInstalled());
    }

    @Test
    void testUpdatesMiddlewareInstalled() {
        assertFalse(connectionManager.getConfig().isMiddlewareInstalled());

        var res = new UpdateAgentDataCommand.Res(null, 500, true, null);
        command.execute(res, connectionManager);

        assertTrue(connectionManager.getConfig().isMiddlewareInstalled());
    }
}
