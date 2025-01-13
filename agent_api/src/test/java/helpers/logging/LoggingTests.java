package helpers.logging;

import dev.aikido.agent_api.helpers.logging.LogLevel;
import dev.aikido.agent_api.helpers.logging.LogManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoggingTests {
    // Test logLevels :
    @Test
    public void testLogLevels() {
        assertTrue(LogLevel.ERROR.getLevel() > LogLevel.INFO.getLevel());
        assertTrue(LogLevel.FATAL.getLevel() > LogLevel.ERROR.getLevel());
        assertTrue(LogLevel.TRACE.getLevel() < LogLevel.DEBUG.getLevel());
        assertTrue(LogLevel.INFO.getLevel() < LogLevel.WARN.getLevel());
    }

    // Test LogManager :
    @Test
    public void testLogManager() {
        assertNotNull(LogManager.getLogger(LoggingTests.class));
        assertNotNull(LogManager.getLogger(LogManager.class));
        assertNull(LogManager.getLogger(null));

    }

    // Test LogManager :

}
