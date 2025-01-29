package wrappers;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import utils.EmptySampleContextObject;

import java.io.File;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static utils.EmtpyThreadCacheObject.getEmptyThreadCacheObject;

public class FileWrapperTest {
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
    @SetEnvironmentVariable(key = "AIKIDO_BLOCK", value = "true")
    @Test
    public void testPathTraversalString() throws Exception {
        setContextAndLifecycle("../file.txt");
        assertThrows(RuntimeException.class, () -> {
            new File("/var/../file.txt");
        });
        cleanup();
        setContextAndLifecycle("/../file.txt");
        assertThrows(RuntimeException.class, () -> {
            new File("/var/../file.txt");
        });
        
        cleanup();
        assertDoesNotThrow(() -> {
            new File("/var/../file.txt");
        });
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCK", value = "true")
    @Test
    public void testPathTraversalURI() throws Exception {
        setContextAndLifecycle("../file.txt");
        assertThrows(RuntimeException.class, () -> {
            new File(new URI("file:///var/../file.txt"));
        });
        cleanup();
        setContextAndLifecycle("/../file.txt");
        assertThrows(RuntimeException.class, () -> {
            new File(new URI("file:///var/../file.txt"));
        });

        cleanup();
        assertDoesNotThrow(() -> {
            new File(new URI("file:///var/../file.txt"));
        });
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCK", value = "true")
    @Test
    public void testPathTraversalMultiple() throws Exception {
        setContextAndLifecycle("../file.txt");
        assertThrows(RuntimeException.class, () -> {
            new File("/var/", "../file.txt");
        });
        cleanup();
        setContextAndLifecycle("/../file.txt");
        assertThrows(RuntimeException.class, () -> {
            new File("/etc/", "/var/../file.txt");
        });
        cleanup();
        setContextAndLifecycle("/../file.txt");
        assertThrows(RuntimeException.class, () -> {
            new File("/../file.txt", "/test/");
        });

        cleanup();
        assertDoesNotThrow(() -> {
            new File("/etc/", "/var/../file.txt");
        });
    }
}