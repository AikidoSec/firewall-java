package background.ipc_commands;


import com.google.gson.Gson;
import dev.aikido.agent_api.Config;
import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.background.ServiceConfiguration;
import dev.aikido.agent_api.background.cloud.CloudConnectionManager;
import dev.aikido.agent_api.background.ipc_commands.ShouldRateLimitCommand;
import dev.aikido.agent_api.context.RouteMetadata;
import dev.aikido.agent_api.context.User;
import dev.aikido.agent_api.ratelimiting.RateLimiter;
import dev.aikido.agent_api.ratelimiting.ShouldRateLimit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShouldRateLimitCommandTest {
    private ShouldRateLimitCommand command;
    private CloudConnectionManager connectionManager;
    private Gson gson;

    @BeforeEach
    void setUp() {
        command = new ShouldRateLimitCommand();
        connectionManager = mock(CloudConnectionManager.class);
        gson = new Gson();
    }

    @Test
    void testExecuteWithValidDataAndUserAllowed() {
        // Arrange
        RouteMetadata routeMetadata = new RouteMetadata("/api/resource", "https://example.com/api/resource", "GET");
        User user1 = new User("1", "User One", "192.168.1.1", System.currentTimeMillis(), System.currentTimeMillis());
        String remoteAddress = "192.168.1.1";

        // Mocking the rate limiting behavior
        ServiceConfiguration config = mock(ServiceConfiguration.class);
        RateLimiter rateLimiter = mock(RateLimiter.class);
        Endpoint endpoint = mock(Endpoint.class);

        when(connectionManager.getConfig()).thenReturn(config);
        when(config.getEndpoints()).thenReturn(Collections.singletonList(endpoint));
        when(endpoint.getRateLimiting()).thenReturn(new Endpoint.RateLimitingConfig(1000, 5, true));
        when(endpoint.getMethod()).thenReturn("GET");
        when(endpoint.getRoute()).thenReturn("/api/resource");
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(true);
        when(connectionManager.getRateLimiter()).thenReturn(rateLimiter);
        // Create the request JSON
        String requestJson = gson.toJson(new ShouldRateLimitCommand.Req(routeMetadata, user1, remoteAddress));

        // Act
        Optional<String> result = command.execute(requestJson, connectionManager);

        // Assert
        assertTrue(result.isPresent());
        ShouldRateLimit.RateLimitDecision response = gson.fromJson(result.get(), ShouldRateLimit.RateLimitDecision.class);
        assertFalse(response.block());
        assertNull(response.trigger());
    }
    @Test
    void testExecuteWithValidDataAndUserBlocked() {
        // Arrange
        RouteMetadata routeMetadata = new RouteMetadata("/api/resource", "https://example.com/api/resource", "GET");
        User user1 = new User("1", "User One", "192.168.1.1", System.currentTimeMillis(), System.currentTimeMillis());
        String remoteAddress = "192.168.1.1";

        // Mocking the rate limiting behavior
        ServiceConfiguration config = mock(ServiceConfiguration.class);
        RateLimiter rateLimiter = mock(RateLimiter.class);
        Endpoint endpoint = mock(Endpoint.class);

        when(connectionManager.getConfig()).thenReturn(config);
        when(config.getEndpoints()).thenReturn(Collections.singletonList(endpoint));
        when(endpoint.getRateLimiting()).thenReturn(new Endpoint.RateLimitingConfig(1000, 5, true));
        when(endpoint.getMethod()).thenReturn("GET");
        when(endpoint.getRoute()).thenReturn("/api/resource");
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(false);
        when(connectionManager.getRateLimiter()).thenReturn(rateLimiter);
        // Create the request JSON
        String requestJson = gson.toJson(new ShouldRateLimitCommand.Req(routeMetadata, user1, remoteAddress));

        // Act
        Optional<String> result = command.execute(requestJson, connectionManager);

        // Assert
        assertTrue(result.isPresent());
        ShouldRateLimit.RateLimitDecision response = gson.fromJson(result.get(), ShouldRateLimit.RateLimitDecision.class);
        assertTrue(response.block());
        assertEquals("user", response.trigger());
    }

    @Test
    void testExecuteWithValidDataAndNoUser() {
        // Arrange
        RouteMetadata routeMetadata = new RouteMetadata("/api/resource", "https://example.com/api/resource", "GET");
        User user = null; // No user
        String remoteAddress = "192.168.1.1";

        // Mocking the configuration and rate limiter
        ServiceConfiguration config = mock(ServiceConfiguration.class);
        RateLimiter rateLimiter = mock(RateLimiter.class);
        Endpoint endpoint = mock(Endpoint.class);

        when(connectionManager.getConfig()).thenReturn(config);
        when(config.getEndpoints()).thenReturn(Collections.singletonList(endpoint));
        when(endpoint.getRateLimiting()).thenReturn(new Endpoint.RateLimitingConfig(1000, 5, true)); // Example values
        when(endpoint.getMethod()).thenReturn("GET");
        when(endpoint.getRoute()).thenReturn("/api/resource");
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(false); // Simulate blocking
        when(connectionManager.getRateLimiter()).thenReturn(rateLimiter);

        // Create the request JSON
        String requestJson = gson.toJson(new ShouldRateLimitCommand.Req(routeMetadata, user, remoteAddress));

        // Act
        Optional<String> result = command.execute(requestJson, connectionManager);

        // Assert
        assertTrue(result.isPresent());
        ShouldRateLimit.RateLimitDecision response = gson.fromJson(result.get(), ShouldRateLimit.RateLimitDecision.class);
        assertTrue(response.block());
        assertEquals("ip", response.trigger()); // Expecting the trigger to be "ip" since there's no user
    }

    @Test
    void testExecuteWithValidDataAndNoUserAndResponseNull() {
        // Arrange
        RouteMetadata routeMetadata = new RouteMetadata("/api/resource", "https://example.com/api/resource", "GET");
        User user = null; // No user
        String remoteAddress = "192.168.1.1";

        // Mocking the configuration and rate limiter
        ServiceConfiguration config = mock(ServiceConfiguration.class);
        RateLimiter rateLimiter = mock(RateLimiter.class);
        Endpoint endpoint = mock(Endpoint.class);

        when(connectionManager.getConfig()).thenReturn(config);
        when(config.getEndpoints()).thenReturn(Collections.singletonList(endpoint));
        when(endpoint.getRateLimiting()).thenReturn(new Endpoint.RateLimitingConfig(1000, 5, true)); // Example values
        when(endpoint.getMethod()).thenReturn("GET");
        when(endpoint.getRoute()).thenReturn("/api/resource");
        when(rateLimiter.isAllowed(anyString(), anyLong(), anyLong())).thenReturn(false); // Simulate blocking
        when(connectionManager.getRateLimiter()).thenReturn(rateLimiter);

        // Create the request JSON
        String requestJson = gson.toJson(new ShouldRateLimitCommand.Req(routeMetadata, user, remoteAddress));

        // Act
        Optional<String> result = command.execute(requestJson, connectionManager);

        // Assert
        assertTrue(result.isPresent());
        ShouldRateLimit.RateLimitDecision response = gson.fromJson(result.get(), ShouldRateLimit.RateLimitDecision.class);
        assertTrue(response.block());
        assertEquals("ip", response.trigger()); // Expecting the trigger to be "ip" since there's no user
    }

}