package helpers;

import dev.aikido.agent_api.helpers.ArrayHelpers;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ArrayHelpersTest {

    @Test
    void testLastElement_WithNonEmptyArray_ReturnsLastElement() {
        String[] array = {"a", "b", "c"};
        assertEquals("c", ArrayHelpers.lastElement(array));
    }

    @Test
    void testLastElement_WithEmptyArray_ReturnsNull() {
        String[] array = {};
        assertNull(ArrayHelpers.lastElement(array));
    }

    @Test
    void testLastElement_WithNullArray_ReturnsNull() {
        assertNull(ArrayHelpers.lastElement(null));
    }

    @Test
    void testLastElement_WithSingleElementArray_ReturnsElement() {
        String[] array = {"only"};
        assertEquals("only", ArrayHelpers.lastElement(array));
    }
}
