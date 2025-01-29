package background.cloud;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import dev.aikido.agent_api.background.ServiceConfiguration;
import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.background.cloud.api.ReportingApiHTTP;
import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.background.cloud.api.events.Started;
import dev.aikido.agent_api.background.users.Users;
import dev.aikido.agent_api.helpers.env.Token;
import dev.aikido.agent_api.ratelimiting.RateLimiter;
import dev.aikido.agent_api.storage.Hostnames;
import dev.aikido.agent_api.storage.Statistics;
import dev.aikido.agent_api.storage.routes.Routes;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CloudConnectionManagerTest {

    private CloudConnectionManager cloudConnectionManager;
    private ReportingApi mockApi;

    @BeforeEach
    void setUp() {
        mockApi = mock(ReportingApiHTTP.class);
        cloudConnectionManager = new CloudConnectionManager(true, new Token("token"), "serverless", mockApi);
    }

    @Test
    void testOnStartReportsEvent() {
        // Arrange
        when(mockApi.report(anyString(), any(APIEvent.class), anyInt()))
                .thenReturn(Optional.of(mock(APIResponse.class)));

        // Act
        cloudConnectionManager.onStart();

        // Assert
        ArgumentCaptor<APIEvent> eventCaptor = ArgumentCaptor.forClass(APIEvent.class);
        verify(mockApi).report(eq("token"), eventCaptor.capture(), eq(10));
    }

    @Test
    void testReportEventUpdatesConfigWhenResponseIsPresent() {
        // Arrange
        APIResponse mockResponse = mock(APIResponse.class);
        when(mockApi.report(anyString(), any(APIEvent.class), anyInt())).thenReturn(Optional.of(mockResponse));

        // Act
        cloudConnectionManager.reportEvent(Started.get(cloudConnectionManager), true);

        // Assert
        ServiceConfiguration config = cloudConnectionManager.getConfig();
    }

    @Test
    void testReportEventDoesNotUpdateConfigWhenResponseIsNotPresent() {
        // Arrange
        when(mockApi.report(anyString(), any(APIEvent.class), anyInt())).thenReturn(Optional.empty());

        // Act
        cloudConnectionManager.reportEvent(Started.get(cloudConnectionManager), true);

        // Assert
        // No interaction with config update
        verify(mockApi).report(anyString(), any(APIEvent.class), anyInt());
    }

    @Test
    void testShouldBlockReturnsConfigValue() {
        // Act
        boolean shouldBlock = cloudConnectionManager.shouldBlock();

        // Assert
        assertTrue(shouldBlock);
    }

    @Test
    void testGetTokenReturnsCorrectToken() {
        // Act
        String token = cloudConnectionManager.getToken();

        // Assert
        assertEquals("token", token);
    }

    @Test
    void testGetRoutesReturnsRoutesInstance() {
        // Act
        Routes routes = cloudConnectionManager.getRoutes();

        // Assert
        assertNotNull(routes);
    }

    @Test
    void testGetRateLimiterReturnsRateLimiterInstance() {
        // Act
        RateLimiter rateLimiter = cloudConnectionManager.getRateLimiter();

        // Assert
        assertNotNull(rateLimiter);
    }

    @Test
    void testGetUsersReturnsUsersInstance() {
        // Act
        Users users = cloudConnectionManager.getUsers();

        // Assert
        assertNotNull(users);
    }

    @Test
    void testGetStatsReturnsNotNull() {
        // Act
        Statistics stats = cloudConnectionManager.getStats();

        // Assert
        assertNotNull(stats);
    }

    @Test
    void testGetHostnamesReturnsNotNull() {
        // Act
        Hostnames hostnames = cloudConnectionManager.getHostnames();

        // Assert
        assertNotNull(hostnames);
    }

    @Test
    void testGetApi() {
        // Act
        ReportingApiHTTP api = cloudConnectionManager.getApi();

        // Assert
        assertEquals(mockApi, api);
    }
}
