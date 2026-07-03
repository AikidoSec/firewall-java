package helpers.env;

import dev.aikido.agent_api.helpers.env.Endpoints;
import dev.aikido.agent_api.helpers.env.Token;
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
        String result = Endpoints.getAikidoAPIEndpoint(null);
        assertEquals("https://custom.aikido.dev/", result);
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_ENDPOINT", value = "https://custom.aikido.dev")
    public void testGetAikidoAPIEndpoint_WithCustomEndpointWithoutTrailingSlash() {
        String result = Endpoints.getAikidoAPIEndpoint(null);
        assertEquals("https://custom.aikido.dev/", result);
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_ENDPOINT", value = "")
    public void testGetAikidoAPIEndpoint_WithEmptyEnvironmentVariable() {
        String result = Endpoints.getAikidoAPIEndpoint(null);
        assertEquals("https://guard.aikido.dev/", result);
    }

    @Test
    public void testGetAikidoAPIEndpoint_WithNullEnvironmentVariable() {
        // No environment variable set, should return default
        String result = Endpoints.getAikidoAPIEndpoint(null);
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
        String result = Endpoints.getAikidoAPIEndpoint(null);
        assertEquals("https://missing-slash.aikido.dev/", result);
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_ENDPOINT", value = "https://missing-slash.aikido.dev/")
    public void testGetAikidoAPIEndpoint_WithTrailingSlashAlreadyPresent() {
        String result = Endpoints.getAikidoAPIEndpoint(null);
        assertEquals("https://missing-slash.aikido.dev/", result);
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_ENDPOINT", value = "https://another-custom.aikido.dev")
    public void testGetAikidoAPIEndpoint_WithAnotherCustomEndpoint() {
        String result = Endpoints.getAikidoAPIEndpoint(null);
        assertEquals("https://another-custom.aikido.dev/", result);
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_ENDPOINT", value = "https://another-custom.aikido.dev/")
    public void testGetAikidoAPIEndpoint_WithAnotherCustomEndpointWithTrailingSlash() {
        String result = Endpoints.getAikidoAPIEndpoint(null);
        assertEquals("https://another-custom.aikido.dev/", result);
    }

    @Test
    public void testGetAikidoAPIEndpoint_WithUSRegionToken() {
        Token token = new Token("AIK_RUNTIME_1_2_US_random");
        String result = Endpoints.getAikidoAPIEndpoint(token);
        assertEquals("https://guard.us.aikido.dev/", result);
    }

    @Test
    public void testGetAikidoAPIEndpoint_WithMERegionToken() {
        Token token = new Token("AIK_RUNTIME_1_2_ME_random");
        String result = Endpoints.getAikidoAPIEndpoint(token);
        assertEquals("https://guard.me.aikido.dev/", result);
    }

    @Test
    public void testGetAikidoAPIEndpoint_WithAURegionToken() {
        Token token = new Token("AIK_RUNTIME_1_2_AU_random");
        String result = Endpoints.getAikidoAPIEndpoint(token);
        assertEquals("https://guard.au.aikido.dev/", result);
    }

    @Test
    public void testGetAikidoAPIEndpoint_WithEURegionToken() {
        Token token = new Token("AIK_RUNTIME_1_2_EU_random");
        String result = Endpoints.getAikidoAPIEndpoint(token);
        assertEquals("https://guard.aikido.dev/", result);
    }

    @Test
    public void testGetAikidoAPIEndpoint_WithOldFormatToken() {
        Token token = new Token("AIK_RUNTIME_1_2_random");
        String result = Endpoints.getAikidoAPIEndpoint(token);
        assertEquals("https://guard.aikido.dev/", result);
    }

    @Test
    public void testGetAikidoAPIEndpoint_WithNonRuntimeToken() {
        Token token = new Token("some-other-token");
        String result = Endpoints.getAikidoAPIEndpoint(token);
        assertEquals("https://guard.aikido.dev/", result);
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_ENDPOINT", value = "https://custom.aikido.dev")
    public void testGetAikidoAPIEndpoint_CustomEndpointTakesPrecedenceOverRegion() {
        Token token = new Token("AIK_RUNTIME_1_2_US_random");
        String result = Endpoints.getAikidoAPIEndpoint(token);
        assertEquals("https://custom.aikido.dev/", result);
    }
}
