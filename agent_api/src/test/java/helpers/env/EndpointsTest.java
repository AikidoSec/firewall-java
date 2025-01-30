package helpers.env;

import dev.aikido.agent_api.helpers.env.Endpoints;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EndpointsTest {
    @Test
    @SetEnvironmentVariable(key = "AIKIDO_ENDPOINT", value = "https://custom.aikido.dev")
    public void testGetAikidoAPIEndpoint_WithCustomEndpoint() {
        String result = Endpoints.getAikidoAPIEndpoint();
        assertEquals("https://custom.aikido.dev/", result);
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_ENDPOINT", value = "https://custom.aikido.dev")
    public void testGetAikidoAPIEndpoint_WithCustomEndpointWithoutTrailingSlash() {
        String result = Endpoints.getAikidoAPIEndpoint();
        assertEquals("https://custom.aikido.dev/", result);
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_ENDPOINT", value = "")
    public void testGetAikidoAPIEndpoint_WithEmptyEnvironmentVariable() {
        String result = Endpoints.getAikidoAPIEndpoint();
        assertEquals("https://guard.aikido.dev/", result);
    }

    @Test
    public void testGetAikidoAPIEndpoint_WithNullEnvironmentVariable() {
        // No environment variable set, should return default
        String result = Endpoints.getAikidoAPIEndpoint();
        assertEquals("https://guard.aikido.dev/", result);
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_REALTIME_ENDPOINT", value = "https://realtime.aikido.dev")
    public void testGetAikidoRealtimeEndpoint_WithCustomEndpoint() {
        String result = Endpoints.getAikidoRealtimeEndpoint();
        assertEquals("https://realtime.aikido.dev/", result);
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_REALTIME_ENDPOINT", value = "https://realtime.aikido.dev")
    public void testGetAikidoRealtimeEndpoint_WithCustomEndpointWithoutTrailingSlash() {
        String result = Endpoints.getAikidoRealtimeEndpoint();
        assertEquals("https://realtime.aikido.dev/", result);
    }

    @Test
    public void testGetAikidoRealtimeEndpoint_WithNullEnvironmentVariable() {
        // No environment variable set, should return default
        String result = Endpoints.getAikidoRealtimeEndpoint();
        assertEquals("https://runtime.aikido.dev/", result);
    }
    @Test
    @SetEnvironmentVariable(key = "AIKIDO_REALTIME_ENDPOINT", value = "https://realtime.aikido.dev/")
    public void testGetAikidoRealtimeEndpoint_WithCustomEndpointWithTrailingSlash() {
        String result = Endpoints.getAikidoRealtimeEndpoint();
        assertEquals("https://realtime.aikido.dev/", result);
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_REALTIME_ENDPOINT", value = "")
    public void testGetAikidoRealtimeEndpoint_WithEmptyEnvironmentVariable() {
        String result = Endpoints.getAikidoRealtimeEndpoint();
        assertEquals("https://runtime.aikido.dev/", result);
    }

    // Additional tests
    @Test
    @SetEnvironmentVariable(key = "AIKIDO_ENDPOINT", value = "https://missing-slash.aikido.dev")
    public void testGetAikidoAPIEndpoint_WithMissingSlash() {
        String result = Endpoints.getAikidoAPIEndpoint();
        assertEquals("https://missing-slash.aikido.dev/", result);
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_ENDPOINT", value = "https://missing-slash.aikido.dev/")
    public void testGetAikidoAPIEndpoint_WithTrailingSlashAlreadyPresent() {
        String result = Endpoints.getAikidoAPIEndpoint();
        assertEquals("https://missing-slash.aikido.dev/", result);
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_ENDPOINT", value = "https://another-custom.aikido.dev")
    public void testGetAikidoAPIEndpoint_WithAnotherCustomEndpoint() {
        String result = Endpoints.getAikidoAPIEndpoint();
        assertEquals("https://another-custom.aikido.dev/", result);
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_ENDPOINT", value = "https://another-custom.aikido.dev/")
    public void testGetAikidoAPIEndpoint_WithAnotherCustomEndpointWithTrailingSlash() {
        String result = Endpoints.getAikidoAPIEndpoint();
        assertEquals("https://another-custom.aikido.dev/", result);
    }
}
