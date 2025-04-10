package storage;
import dev.aikido.agent_api.context.RouteMetadata;
import org.junit.jupiter.api.Test;

import static dev.aikido.agent_api.storage.routes.RouteToKeyHelper.routeToKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
public class RouteToKeyHelperTest {
    @Test
    public void testRouteToKey_withGetMethod() {
        // Arrange
        RouteMetadata routeMetadata = new RouteMetadata("/api/resource", "http://example.com/api/resource", "GET");

        // Act
        String result = routeToKey(routeMetadata);

        // Assert
        assertEquals("GET:/api/resource", result);
    }

    @Test
    public void testRouteToKey_withPostMethod() {
        // Arrange
        RouteMetadata routeMetadata = new RouteMetadata("/api/resource", "http://example.com/api/resource", "POST");

        // Act
        String result = routeToKey(routeMetadata);

        // Assert
        assertEquals("POST:/api/resource", result);
    }

    @Test
    public void testRouteToKey_withPutMethod() {
        // Arrange
        RouteMetadata routeMetadata = new RouteMetadata("/api/resource", "http://example.com/api/resource", "PUT");

        // Act
        String result = routeToKey(routeMetadata);

        // Assert
        assertEquals("PUT:/api/resource", result);
    }

    @Test
    public void testRouteToKey_withDeleteMethod() {
        // Arrange
        RouteMetadata routeMetadata = new RouteMetadata("/api/resource", "http://example.com/api/resource", "DELETE");

        // Act
        String result = routeToKey(routeMetadata);

        // Assert
        assertEquals("DELETE:/api/resource", result);
    }

    @Test
    public void testRouteToKey_withEmptyRoute() {
        // Arrange
        RouteMetadata routeMetadata = new RouteMetadata("", "http://example.com/api/resource", "GET");

        // Act
        String result = routeToKey(routeMetadata);

        // Assert
        assertEquals("GET:", result);
    }

    @Test
    public void testRouteToKey_withNullMethod() {
        // Arrange
        RouteMetadata routeMetadata = new RouteMetadata("/api/resource", "http://example.com/api/resource", null);

        // Act
        String result = routeToKey(routeMetadata);

        // Assert
        assertEquals("null:/api/resource", result);
    }
}
