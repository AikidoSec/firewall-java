package helpers.logging;

import dev.aikido.agent_api.collectors.FileCollector;
import dev.aikido.agent_api.helpers.logging.LogLevel;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;

import static org.junit.jupiter.api.Assertions.*;

public class LoggingTests {
    // Test logLevels :
    @Test
    public void testLogLevels() {
        assertTrue(LogLevel.ERROR.getLevel() > LogLevel.INFO.getLevel());
        assertTrue(LogLevel.FATAL.getLevel() > LogLevel.ERROR.getLevel());
        assertTrue(LogLevel.TRACE.getLevel() < LogLevel.DEBUG.getLevel());
        assertTrue(LogLevel.INFO.getLevel() < LogLevel.WARN.getLevel());
        assertEquals(0, LogLevel.TRACE.getLevel());
        assertEquals("TRACE", LogLevel.TRACE.toString());
        assertEquals(LogLevel.INFO, LogLevel.valueOf("INFO"));

    }

    // Test LogManager :
    @Test
    public void testLogManager() {
        assertNotNull(LogManager.getLogger(LoggingTests.class));
        assertNotNull(LogManager.getLogger(LogManager.class));
        assertNull(LogManager.getLogger(null));

    }

    // Test Logger
    @Test
    @SetEnvironmentVariable(key = "AIKIDO_LOG_LEVEL", value = "info")
    @StdIo
    public void testLoggerReadsEnv1(StdOut out) {
        // Check it reads environment variable
        Logger logger = new Logger(FileCollector.class);
        logger.info("TEST1");
        logger.trace("TEST2");
        logger.debug("TEST3");
        logger.error("TEST4");
        logger.fatal("TEST5");
        logger.warn("TEST6");

        assertTrue(out.capturedString().contains("INFO dev.aikido.agent_api.collectors.FileCollector: TEST1"));
        assertTrue(out.capturedString().contains("ERROR dev.aikido.agent_api.collectors.FileCollector: TEST4"));
        assertTrue(out.capturedString().contains("FATAL dev.aikido.agent_api.collectors.FileCollector: TEST5"));
        assertTrue(out.capturedString().contains("WARN dev.aikido.agent_api.collectors.FileCollector: TEST6"));
        assertFalse(out.capturedString().contains("TEST2"));
        assertFalse(out.capturedString().contains("TEST3"));
    }
    @Test
    @SetEnvironmentVariable(key = "AIKIDO_LOG_LEVEL", value = "fatal")
    @StdIo
    public void testLoggerReadsEnv2(StdOut out) {
        // Check it reads environment variable
        Logger logger = new Logger(FileCollector.class);
        logger.info("TEST1");
        logger.trace("TEST2");
        logger.debug("TEST3");
        logger.error("TEST4");
        logger.fatal("TEST5");
        logger.warn("TEST6");

        assertTrue(out.capturedString().contains("FATAL dev.aikido.agent_api.collectors.FileCollector: TEST5"));
        assertFalse(out.capturedString().contains("TEST1"));
        assertFalse(out.capturedString().contains("TEST2"));
        assertFalse(out.capturedString().contains("TEST3"));
        assertFalse(out.capturedString().contains("TEST4"));
        assertFalse(out.capturedString().contains("TEST6"));
    }

    @Test
    @StdIo
    public void testLoggerUsesProvidedLevel1(StdOut out) {
        // Check it reads environment variable
        Logger logger = new Logger(FileCollector.class, LogLevel.INFO);
        logger.info("TEST1");
        logger.trace("TEST2");
        logger.debug("TEST3");
        logger.error("TEST4");
        logger.fatal("TEST5");
        logger.warn("TEST6");

        assertTrue(out.capturedString().contains("INFO dev.aikido.agent_api.collectors.FileCollector: TEST1"));
        assertTrue(out.capturedString().contains("ERROR dev.aikido.agent_api.collectors.FileCollector: TEST4"));
        assertTrue(out.capturedString().contains("FATAL dev.aikido.agent_api.collectors.FileCollector: TEST5"));
        assertTrue(out.capturedString().contains("WARN dev.aikido.agent_api.collectors.FileCollector: TEST6"));
        assertFalse(out.capturedString().contains("TEST2"));
        assertFalse(out.capturedString().contains("TEST3"));
    }
    @Test
    @StdIo
    public void testLoggerUsesProvidedLevel2(StdOut out) {
        // Check it reads environment variable
        Logger logger = new Logger(FileCollector.class, LogLevel.FATAL);
        logger.info("TEST1");
        logger.trace("TEST2");
        logger.debug("TEST3");
        logger.error("TEST4");
        logger.fatal("TEST5");
        logger.warn("TEST6");

        assertTrue(out.capturedString().contains("FATAL dev.aikido.agent_api.collectors.FileCollector: TEST5"));
        assertFalse(out.capturedString().contains("TEST1"));
        assertFalse(out.capturedString().contains("TEST2"));
        assertFalse(out.capturedString().contains("TEST3"));
        assertFalse(out.capturedString().contains("TEST4"));
        assertFalse(out.capturedString().contains("TEST6"));
    }

}
