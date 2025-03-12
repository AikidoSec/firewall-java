package background.cloud;

import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.background.cloud.api.ReportingApiHTTP;
import dev.aikido.agent_api.background.cloud.api.events.APIEvent;
import dev.aikido.agent_api.background.cloud.api.events.Started;
import dev.aikido.agent_api.background.users.UsersStore;
import dev.aikido.agent_api.context.User;
import dev.aikido.agent_api.helpers.env.Token;
import dev.aikido.agent_api.ratelimiting.RateLimiter;
import dev.aikido.agent_api.storage.*;
import dev.aikido.agent_api.storage.routes.RouteEntry;
import dev.aikido.agent_api.storage.routes.RoutesStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        when(mockApi.report(anyString(), any(APIEvent.class))).thenReturn(Optional.of(mock(APIResponse.class)));

        // Act
        cloudConnectionManager.onStart();

        // Assert
        ArgumentCaptor<APIEvent> eventCaptor = ArgumentCaptor.forClass(APIEvent.class);
        verify(mockApi).report(eq("token"), eventCaptor.capture());
    }

    @Test
    void testReportEventUpdatesConfigWhenResponseIsPresent() {
        // Arrange
        APIResponse mockResponse = mock(APIResponse.class);
        when(mockApi.report(anyString(), any(APIEvent.class))).thenReturn(Optional.of(mockResponse));

        // Act
        cloudConnectionManager.reportEvent(Started.get(cloudConnectionManager), true);
    }

    @Test
    void testReportEventDoesNotUpdateConfigWhenResponseIsNotPresent() {
        // Arrange
        when(mockApi.report(anyString(), any(APIEvent.class))).thenReturn(Optional.empty());

        // Act
        cloudConnectionManager.reportEvent(Started.get(cloudConnectionManager), true);

        // Assert
        // No interaction with config update
        verify(mockApi).report(anyString(), any(APIEvent.class));
    }

    @Test
    void testShouldBlockReturnsConfigValue() {
        // Act
        boolean shouldBlock = ConfigStore.getConfig().isBlockingEnabled();

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
        RouteEntry[] routes = RoutesStore.getRoutesAsList();

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
        List<User> users = UsersStore.getUsersAsList();

        // Assert
        assertNotNull(users);
    }

    @Test
    void testGetStatsReturnsNotNull() {
        // Act
        Statistics.StatsRecord stats = StatisticsStore.getStatsRecord();

        // Assert
        assertNotNull(stats);
    }

    @Test
    void testGetHostnamesReturnsNotNull() {
        // Act
        Hostnames.HostnameEntry[] hostnames = HostnamesStore.getHostnamesAsList();

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