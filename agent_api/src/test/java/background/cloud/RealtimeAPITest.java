package background.cloud;

import static org.junit.jupiter.api.Assertions.*;

import dev.aikido.agent_api.background.cloud.RealtimeAPI;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

public class RealtimeAPITest {
    private RealtimeAPI realtimeAPI;

    @BeforeEach
    public void setUp() {
        realtimeAPI = new RealtimeAPI();
    }

    @SetEnvironmentVariable(key = "AIKIDO_REALTIME_ENDPOINT", value = "http://localhost:5000/realtime")
    @Test
    public void testGetConfigSuccess() throws Exception {
        String token = "Bearer testToken";

        Optional<RealtimeAPI.ConfigResponse> response = realtimeAPI.getConfig(token);

        assertTrue(response.isPresent());
        assertEquals(0, response.get().configUpdatedAt());
    }

    @SetEnvironmentVariable(key = "AIKIDO_REALTIME_ENDPOINT", value = "http://localnothost:2500")
    @Test
    public void testURLNotAvailable() throws Exception {
        String token = "Bearer testToken";

        Optional<RealtimeAPI.ConfigResponse> response = realtimeAPI.getConfig(token);

        assertFalse(response.isPresent());
    }

    @SetEnvironmentVariable(key = "AIKIDO_REALTIME_ENDPOINT", value = "")
    @Test
    public void testNot200OK() throws Exception {
        String token = "Bearer testToken";

        Optional<RealtimeAPI.ConfigResponse> response = realtimeAPI.getConfig(token);

        assertFalse(response.isPresent());
    }
}
