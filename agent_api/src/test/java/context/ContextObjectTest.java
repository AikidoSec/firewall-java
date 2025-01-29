package context;

import static org.junit.jupiter.api.Assertions.*;

import dev.aikido.agent_api.context.ContextObject;
import org.junit.jupiter.api.Test;

class ContextObjectTest {

    @Test
    void testSetAndGetBody() {
        // Arrange
        ContextObject contextObject = new ContextObject();
        Object expectedBody = "This is a test body";

        // Act
        contextObject.setBody(expectedBody);
        Object actualBody = contextObject.getBody();

        // Assert
        assertEquals(expectedBody, actualBody);
    }
}
