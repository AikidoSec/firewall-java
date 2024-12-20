package context;

import dev.aikido.agent_api.context.ContextObject;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

class ContextObjectTest {

    @Test
    void testSetAndGetBody() {
        // Arrange
        ContextObject contextObject = new ContextObject();
        Object expectedBody = "This is a test body";

        // Act
        contextObject.setBody((Serializable) expectedBody);
        Object actualBody = contextObject.getBody();

        // Assert
        assertEquals(expectedBody, actualBody);
    }
}

