package background;

import dev.aikido.agent_api.background.RealtimeTask;
import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.RealtimeAPI;
import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApiHTTP;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.util.Optional;

import static org.mockito.Mockito.*;

class RealtimeTaskTest {
    private CloudConnectionManager connectionManager;
    private ReportingApiHTTP reportingApiHTTP;
    private RealtimeAPI realtimeAPI;
    private RealtimeTask realtimeTask;

    @BeforeEach
    void setUp() {
        connectionManager = mock(CloudConnectionManager.class);

        reportingApiHTTP = mock(ReportingApiHTTP.class);
        when(connectionManager.getApi()).thenReturn(reportingApiHTTP);

        realtimeTask = new RealtimeTask(connectionManager);
    }

    @SetEnvironmentVariable(key = "AIKIDO_REALTIME_ENDPOINT", value="http://localhost:5000/realtime")
    @Test
    void testRunWithUpdatedConfig() {
        // Arrange
        String token = "test-token";
        when(connectionManager.getToken()).thenReturn(token);

        // Mock the response from RealtimeAPI
        RealtimeAPI.ConfigResponse configResponse = mock(RealtimeAPI.ConfigResponse.class);
        // Mock the API response for fetching new config
        APIResponse apiResponse = mock(APIResponse.class);
        when(reportingApiHTTP.fetchNewConfig(token)).thenReturn(Optional.of(apiResponse));

        // Act
        realtimeTask.run();
    }
}
