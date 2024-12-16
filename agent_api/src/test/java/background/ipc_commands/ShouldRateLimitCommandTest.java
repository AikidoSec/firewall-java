package background.ipc_commands;


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

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShouldRateLimitCommandTest {
    private ShouldRateLimitCommand command;
    private CloudConnectionManager connectionManager;

    @BeforeEach
    void setUp() {
        command = new ShouldRateLimitCommand();
        connectionManager = mock(CloudConnectionManager.class);
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
        ShouldRateLimitCommand.Req req = new ShouldRateLimitCommand.Req(routeMetadata, user1, remoteAddress);

        // Act
        Optional<ShouldRateLimit.RateLimitDecision> result = command.execute(req, connectionManager);

        // Assert
        assertTrue(result.isPresent());
        assertFalse(result.get().block());
        assertNull(result.get().trigger());
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
        ShouldRateLimitCommand.Req req = new ShouldRateLimitCommand.Req(routeMetadata, user1, remoteAddress);

        // Act
        Optional<ShouldRateLimit.RateLimitDecision> result = command.execute(req, connectionManager);

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get().block());
        assertEquals("user", result.get().trigger());
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
        ShouldRateLimitCommand.Req req = new ShouldRateLimitCommand.Req(routeMetadata, user, remoteAddress);

        // Act
        Optional<ShouldRateLimit.RateLimitDecision> result = command.execute(req, connectionManager);

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get().block());
        assertEquals("ip", result.get().trigger()); // Expecting the trigger to be "ip" since there's no user
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
        ShouldRateLimitCommand.Req req = new ShouldRateLimitCommand.Req(routeMetadata, user, remoteAddress);

        // Act
        Optional<ShouldRateLimit.RateLimitDecision> result = command.execute(req, connectionManager);

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get().block());
        assertEquals("ip", result.get().trigger()); // Expecting the trigger to be "ip" since there's no user
    }

}