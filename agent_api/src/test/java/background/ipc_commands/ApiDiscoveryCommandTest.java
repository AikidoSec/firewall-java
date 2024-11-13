package background.ipc_commands;

import dev.aikido.agent_api.api_discovery.APISpec;
import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.background.ipc_commands.CommandRouter;
import dev.aikido.agent_api.context.RouteMetadata;
import dev.aikido.agent_api.helpers.env.Token;
import dev.aikido.agent_api.storage.routes.RouteEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void testApiDiscoveryCommandExecution() {
        RouteMetadata routeMetadata = new RouteMetadata("/api/test", "localhost:5000/api/test", "GET"); // Create a valid RouteMetadata object
        connectionManager.getRoutes().initializeRoute(routeMetadata);

        // Create the input string for the command
        String input = "API_DISCOVERY${\"apiSpec\": {\"body\": null, \"query\": null, \"auth\": null}, \"routeMetadata\": {\"method\": \"GET\", \"route\": \"/api/test\"}}";
        assertNull(connectionManager.getRoutes().get(routeMetadata).apispec, "Expected API spec to be null before update");

        // Execute the command
        Optional<String> result = commandRouter.parseIPCInput(input);

        // Verify the result
        assertTrue(result.isEmpty(), "Expected no result from API_DISCOVERY command");

        // Verify that the routeEntry's API spec was updated
        assertNotNull(connectionManager.getRoutes().get(routeMetadata).apispec, "Expected API spec to be updated");
        // You can add more assertions to check the contents of the updated API spec
    }
    @Test
    void testApiDiscoveryCommandWithInvalidRoute() {
        RouteMetadata routeMetadata = new RouteMetadata("/api/nonexistent", "localhost:5000/api/test", "GET"); // Create a valid RouteMetadata object

        // Create the input string for the command
        String input = "API_DISCOVERY${\"apiSpec\": {}, \"routeMetadata\": {\"method\": \"GET\", \"route\": \"/api/nonexistent\"}}";

        // Execute the command
        Optional<String> result = commandRouter.parseIPCInput(input);

        // Verify the result
        assertTrue(result.isEmpty(), "Expected no result from API_DISCOVERY command with invalid route");
        assertNull(connectionManager.getRoutes().get(routeMetadata));
    }
    @Test
    void testApiDiscoveryCommandWithMalformedInput() {
        // Create a malformed input string
        String input = "API_DISCOVERY${malformed_json";

        // Execute the command
        Optional<String> result = commandRouter.parseIPCInput(input);

        // Verify the result
        assertTrue(result.isEmpty(), "Expected no result from malformed API_DISCOVERY command");
    }
    @Test
    void testApiDiscoveryCommandWithEmptyJson() {
        // Create a malformed input string
        String input = "API_DISCOVERY${\"routeMetadata\": null, \"apiSpec\": null}";

        // Execute the command
        Optional<String> result = commandRouter.parseIPCInput(input);

        // Verify the result
        assertTrue(result.isEmpty(), "Expected no result from malformed API_DISCOVERY command");
    }
}
