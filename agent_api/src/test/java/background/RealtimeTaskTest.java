package background;

import dev.aikido.agent_api.background.RealtimeTask;
import dev.aikido.agent_api.background.ServiceConfiguration;
import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.RealtimeAPI;
import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApiHTTP;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RealtimeTaskTest {
    private CloudConnectionManager connectionManager;
    private ReportingApiHTTP reportingApiHTTP;
    private ServiceConfiguration serviceConfiguration;
    private RealtimeAPI realtimeAPI;
    private RealtimeTask realtimeTask;

    @BeforeEach
    void setUp() {
        connectionManager = mock(CloudConnectionManager.class);

        reportingApiHTTP = mock(ReportingApiHTTP.class);
        serviceConfiguration = mock(ServiceConfiguration.class);
        when(connectionManager.getApi()).thenReturn(reportingApiHTTP);
        when(connectionManager.getConfig()).thenReturn(serviceConfiguration);

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

        // Assert
        // Verify that the config was updated
        verify(serviceConfiguration).updateConfig(apiResponse);
    }

    @SetEnvironmentVariable(key = "AIKIDO_REALTIME_ENDPOINT", value="http://localhost:5000/realtime")
    @Test
    void testRunWithNoConfigUpdate() {
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

        // Assert
        // Verify that the config was updated
        verify(serviceConfiguration).updateConfig(apiResponse);
        // Act again
        reset(serviceConfiguration);
        realtimeTask.run();

        // Assert
        // Verify that the config was not updated again
        verify(connectionManager.getConfig(), never()).updateConfig(any());
    }

    @Test
    void testRunWithEmptyResponse() {
        // Arrange
        String token = "test-token";
        when(connectionManager.getToken()).thenReturn(token);
        when(reportingApiHTTP.fetchNewConfig(token)).thenReturn(Optional.empty());
        // Act
        realtimeTask.run();

        // Assert
        // Verify that no config update was attempted
        verify(connectionManager.getConfig(), never()).updateConfig(any());
    }
}
