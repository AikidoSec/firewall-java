import dev.aikido.agent_api.helpers.logging.LogLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LogLevelTest {

    @Test
    public void testLogLevelValues() {
        assertEquals(0, LogLevel.TRACE.getLevel());
        assertEquals(1, LogLevel.DEBUG.getLevel());
        assertEquals(2, LogLevel.INFO.getLevel());
        assertEquals(3, LogLevel.WARN.getLevel());
        assertEquals(4, LogLevel.ERROR.getLevel());
        assertEquals(5, LogLevel.FATAL.getLevel());
    }

    @Test
    public void testLogLevelToString() {
        assertEquals("TRACE", LogLevel.TRACE.toString());
        assertEquals("DEBUG", LogLevel.DEBUG.toString());
        assertEquals("INFO", LogLevel.INFO.toString());
        assertEquals("WARN", LogLevel.WARN.toString());
        assertEquals("ERROR", LogLevel.ERROR.toString());
        assertEquals("FATAL", LogLevel.FATAL.toString());
    }
}
