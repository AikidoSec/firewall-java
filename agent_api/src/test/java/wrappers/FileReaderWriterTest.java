package wrappers;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import utils.EmptySampleContextObject;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;

import static org.junit.jupiter.api.Assertions.*;
import static utils.EmtpyThreadCacheObject.getEmptyThreadCacheObject;

public class FileReaderWriterTest {
    @AfterEach
    void cleanup() {
        Context.set(null);
        ThreadCache.set(null);
    }
    @BeforeEach
    void clearThreadCache() {
        cleanup();
        ThreadCache.set(getEmptyThreadCacheObject());
        String prop = System.getProperty("AIK_INTERNAL_coverage_run");
        Assumptions.assumeFalse(prop != null && prop.equals("1"), "With coverage enabled we skip File(...) test cases.");
    }
    private void setContextAndLifecycle(String url) {
        Context.set(new EmptySampleContextObject(url));
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    public void testFileReader() throws Exception {
        setContextAndLifecycle("../file.txt");
        Exception exception1 = assertThrows(RuntimeException.class, () -> {
            new FileReader("/var/../file.txt");
        });
        assertEquals("Aikido Zen has blocked Path Traversal", exception1.getMessage());

        cleanup();
        setContextAndLifecycle("/../file.txt");
        Exception exception2 = assertThrows(RuntimeException.class, () -> {
            new FileReader("/var/../file.txt");
        });
        assertEquals("Aikido Zen has blocked Path Traversal", exception2.getMessage());
        
        cleanup();
        assertThrows(FileNotFoundException.class, () -> {
            new FileReader("/var/../file.txt");
        });
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    public void testFileWriter() throws Exception {
        setContextAndLifecycle("../file.txt");
        Exception exception1 = assertThrows(RuntimeException.class, () -> {
            new FileWriter("/var/../file.txt");
        });
        assertEquals("Aikido Zen has blocked Path Traversal", exception1.getMessage());

        cleanup();
        setContextAndLifecycle("/../file.txt");
        Exception exception2 = assertThrows(RuntimeException.class, () -> {
            new FileWriter("/var/../file.txt");
        });
        assertEquals("Aikido Zen has blocked Path Traversal", exception2.getMessage());

        cleanup();
        assertThrows(FileNotFoundException.class, () -> {
            new FileWriter("/var/../file.txt");
        });
    }

}