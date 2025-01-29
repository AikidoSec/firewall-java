package helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.helpers.extraction.UserAgentFromContext;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class UserAgentFromContextTest {

    @Test
    public void testGetUserAgent_WithValidUserAgent() {
        // Arrange
        ContextObject mockContext = Mockito.mock(ContextObject.class);
        HashMap<String, String> headers = new HashMap<>();
        headers.put("user-agent", "Mozilla/5.0");
        Mockito.when(mockContext.getHeaders()).thenReturn(headers);

        // Act
        String userAgent = UserAgentFromContext.getUserAgent(mockContext);

        // Assert
        assertEquals("Mozilla/5.0", userAgent);
    }

    @Test
    public void testGetUserAgent_WithDifferentCaseUserAgent() {
        // Arrange
        ContextObject mockContext = Mockito.mock(ContextObject.class);
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0");
        Mockito.when(mockContext.getHeaders()).thenReturn(headers);

        // Act
        String userAgent = UserAgentFromContext.getUserAgent(mockContext);

        // Assert
        assertEquals("Mozilla/5.0", userAgent);
    }

    @Test
    public void testGetUserAgent_WithoutUserAgent() {
        // Arrange
        ContextObject mockContext = Mockito.mock(ContextObject.class);
        HashMap<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/json");
        Mockito.when(mockContext.getHeaders()).thenReturn(headers);

        // Act
        String userAgent = UserAgentFromContext.getUserAgent(mockContext);

        // Assert
        assertEquals("Unknown User Agent", userAgent);
    }

    @Test
    public void testGetUserAgent_EmptyHeaders() {
        // Arrange
        ContextObject mockContext = Mockito.mock(ContextObject.class);
        HashMap<String, String> headers = new HashMap<>();
        Mockito.when(mockContext.getHeaders()).thenReturn(headers);

        // Act
        String userAgent = UserAgentFromContext.getUserAgent(mockContext);

        // Assert
        assertEquals("Unknown User Agent", userAgent);
    }
}
