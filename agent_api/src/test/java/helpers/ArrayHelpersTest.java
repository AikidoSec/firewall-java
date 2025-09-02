package helpers;

import dev.aikido.agent_api.helpers.ArrayHelpers;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ArrayHelpersTest {

    @Test
    void testPop_WithNonEmptyArray_ReturnsLastElement() {
        String[] array = {"a", "b", "c"};
        assertEquals("c", ArrayHelpers.pop(array));
    }

    @Test
    void testPop_WithEmptyArray_ReturnsNull() {
        String[] array = {};
        assertNull(ArrayHelpers.pop(array));
    }

    @Test
    void testPop_WithNullArray_ReturnsNull() {
        assertNull(ArrayHelpers.pop(null));
    }

    @Test
    void testPop_WithSingleElementArray_ReturnsElement() {
        String[] array = {"only"};
        assertEquals("only", ArrayHelpers.pop(array));
    }
}
