package helpers;

import dev.aikido.agent_api.collectors.FileCollector;
import dev.aikido.agent_api.helpers.logging.LogLevel;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LoggingTest {
    Logger logger;
    @BeforeEach
    public void setup() {
        logger = new Logger(FileCollector.class, LogLevel.TRACE);
    }
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
        assertNotNull(LogManager.getLogger(LoggingTest.class));
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

    @Test
    @StdIo
    public void testLoggerCanHandleArraysAndCollections(StdOut out) {
        Logger logger = new Logger(FileCollector.class, LogLevel.DEBUG);
        logger.info("Test %s %s", "String here", (new Integer[]{1, 6,2, 3}));
        assertTrue(out.capturedString().contains("INFO dev.aikido.agent_api.collectors.FileCollector: Test String here [1, 6, 2, 3]"));

        logger.info("Test %s %s", "String2", (new String[]{"Hello", "World"}));
        assertTrue(out.capturedString().contains("INFO dev.aikido.agent_api.collectors.FileCollector: Test String2 [Hello, World]"));

        logger.info("Test %s %s", "String2", List.of("Hiya", "2"));
        assertTrue(out.capturedString().contains("INFO dev.aikido.agent_api.collectors.FileCollector: Test String2 [Hiya,2]"));
    }

    @Test
    public void testParseArgumentsWithNewline() {
        Object[] args = {"input_with_newline\nnext_line"};
        List<Object> result = logger.parseArguments(args);
        assertEquals(Arrays.asList("input_with_newlinenext_line"), result, "Should remove newline characters.");
    }

    @Test
    public void testParseArgumentsWithCarriageReturn() {
        Object[] args = {"input_with_carriage_return\rnext_line"};
        List<Object> result = logger.parseArguments(args);
        assertEquals(Arrays.asList("input_with_carriage_returnnext_line"), result, "Should remove carriage return characters.");
    }

    @Test
    public void testParseArgumentsWithTab() {
        Object[] args = {"input_with_tab\tand_more"};
        List<Object> result = logger.parseArguments(args);
        // Tab is allowed :
        assertEquals(Arrays.asList("input_with_tab\tand_more"), result);
    }

    @Test
    public void testParseArgumentsWithMultipleSpecialCharacters() {
        Object[] args = {"Newline \n here", "carriage return \r here"};
        List<Object> result = logger.parseArguments(args);
        assertArrayEquals(new String[]{"Newline  here", "carriage return  here"}, result.toArray());
    }

    @Test
    public void testParseArgumentsWithArrayContainingNewline() {
        Object[] args = {new String[]{"value1", "value2\nvalue3"}};
        List<Object> result = logger.parseArguments(args);
        assertArrayEquals(new String[]{"[value1, value2value3]"}, result.toArray());
    }

    @Test
    public void testParseArgumentsWithCollectionContainingCarriageReturn() {
        Object[] args = {Arrays.asList("value1", "value2\rvalue3")};
        List<Object> result = logger.parseArguments(args);
        assertArrayEquals(new String[]{"[value1,value2value3]"}, result.toArray());
    }

    @Test
    public void testParseArgumentsWithMixedInputIncludingSpecialChars() {
        Object[] args = {"normal_input", "malicious_input; DROP TABLE users; --", "input_with_newline\nnext_line"};
        List<Object> result = logger.parseArguments(args);
        assertArrayEquals(new String[]{"normal_input", "malicious_input; DROP TABLE users; --", "input_with_newlinenext_line"}, result.toArray());
    }

}
