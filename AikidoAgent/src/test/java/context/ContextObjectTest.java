package context;

import dev.aikido.AikidoAgent.context.ContextObject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

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

