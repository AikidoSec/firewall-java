package context;

import dev.aikido.agent_api.context.ContextObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    public static class TestContextObject extends ContextObject {

        // Constructor
        public TestContextObject() {
            this.headers = new HashMap<>();
        }

        // Method to set headers for testing
        public void setHeaders(HashMap<String, List<String>> headers) {
            this.headers = headers;
        }
    }

    private TestContextObject context;

    @BeforeEach
    public void setUp() {
        context = new TestContextObject();
    }

    @Test
    public void testGetHeaderReturnsCorrectValue() {
        List<String> contentType = new ArrayList<>();
        contentType.add("application/json");
        context.getHeaders().put("Content-Type", contentType);

        String result = context.getHeader("Content-Type");
        assertEquals("application/json", result);
    }

    @Test
    public void testGetHeaderReturnsNullForNonExistentHeader() {
        String result = context.getHeader("Non-Existent-Header");
        assertNull(result);
    }

    @Test
    public void testGetHeaderIsCaseInsensitive() {
        List<String> accept = new ArrayList<>();
        accept.add("text/html");
        context.getHeaders().put("Accept", accept);

        String result = context.getHeader("accept");
        assertEquals("text/html", result);
    }

    @Test
    public void testGetHeaderReturnsLastValueWhenMultipleValuesPresent() {
        List<String> multipleValues = new ArrayList<>();
        multipleValues.add("value1");
        multipleValues.add("value2");
        context.getHeaders().put("Custom-Header", multipleValues);

        String result = context.getHeader("Custom-Header");
        assertEquals("value2", result);
    }

    @Test
    public void testGetHeaderReturnsNullForEmptyHeaderList() {
        List<String> emptyList = new ArrayList<>();
        context.getHeaders().put("Empty-Header", emptyList);

        String result = context.getHeader("Empty-Header");
        assertNull(result);
    }

    @Test
    public void testOptionalForcedProtectionOffIsEmpty() {
        assertTrue(context.getForcedProtectionOff().isEmpty());
    }

    @Test
    public void testSettingForcedProtectionOff() {
        context.setForcedProtectionOff(true);
        assertTrue(context.getForcedProtectionOff().get());

        context.setForcedProtectionOff(false);
        assertFalse(context.getForcedProtectionOff().get());
    }
}

