package background.ipc_commands;

import dev.aikido.agent_api.api_discovery.APISpec;
import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.background.ipc_commands.ApiDiscoveryCommand;
import dev.aikido.agent_api.background.ipc_commands.Command;
import dev.aikido.agent_api.background.ipc_commands.CommandRouter;
import dev.aikido.agent_api.context.RouteMetadata;
import dev.aikido.agent_api.helpers.env.Token;
import dev.aikido.agent_api.storage.routes.RouteEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class ApiDiscoveryCommandTest {

    private CommandRouter commandRouter;
    private CloudConnectionManager connectionManager;
    private BlockingQueue<APIEvent> queue;

    @BeforeEach
    void setUp() {
        // Initialize the CloudConnectionManager and BlockingQueue
        queue = new ArrayBlockingQueue<APIEvent>(10);
        connectionManager = new CloudConnectionManager(true, new Token("token"), null);
        commandRouter = new CommandRouter(connectionManager, queue);
    }
    
    @Test
    void testApiDiscoveryCommandWithInvalidRoute() {
        RouteMetadata routeMetadata = new RouteMetadata("/api/nonexistent", "localhost:5000/api/test", "GET"); // Create a valid RouteMetadata object

        // Create the input string for the command
        ApiDiscoveryCommand.Req req = new ApiDiscoveryCommand.Req(new APISpec(null, null, null), new RouteMetadata("/api/nonexistant", "/api/nonexistant", "GET"));

        // Execute the command
        Optional<Command.EmptyResult> result = new ApiDiscoveryCommand().execute(req, connectionManager);

        // Verify the result
        assertTrue(result.isEmpty(), "Expected no result from malformed API_DISCOVERY command");
        assertNull(connectionManager.getRoutes().get(routeMetadata));
    }
    @Test
    void testApiDiscoveryCommandWithMalformedInput() {
        // Create a malformed input string
        String input = "API_DISCOVERY$malformed_data";

        // Execute the command
        Optional<byte[]> result = commandRouter.parseIPCInput(input.getBytes(StandardCharsets.UTF_8));

        // Verify the result
        assertTrue(result.isEmpty(), "Expected no result from malformed API_DISCOVERY command");
    }
    @Test
    void testApiDiscoveryCommandWithEmptyJson() {
        // Create a malformed input string
        ApiDiscoveryCommand.Req req = new ApiDiscoveryCommand.Req(null, null);
        // Execute the command
        Optional<Command.EmptyResult> result = new ApiDiscoveryCommand().execute(req, connectionManager);

        // Verify the result
        assertTrue(result.isEmpty(), "Expected no result from malformed API_DISCOVERY command");
    }
}
