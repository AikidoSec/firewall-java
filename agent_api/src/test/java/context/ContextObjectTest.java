package context;

import dev.aikido.agent_api.context.ContextObject;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ContextObjectTest {

    @Test
    void testSetAndGetBody() {
        // Arrange
        ContextObject contextObject = new ContextObject();
        Object expectedBody = "This is a test body";

        // Act
        contextObject.setBodyElement("key1", (Serializable) expectedBody);
        Map<String, Object> actualBody = contextObject.getBody();

        // Assert
        assertEquals(1, actualBody.size());
        assertEquals(expectedBody, actualBody.get("key1"));
    }
}

