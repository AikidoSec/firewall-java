package wrappers;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import utils.EmptySampleContextObject;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static utils.EmtpyThreadCacheObject.getEmptyThreadCacheObject;

public class PathsWrapperTest {
    @AfterEach
    void cleanup() {
        Context.set(null);
        ThreadCache.set(null);
    }
    @BeforeEach
    void clearThreadCache() {
        cleanup();
        ThreadCache.set(getEmptyThreadCacheObject());
    }
    private void setContextAndLifecycle(String url) {
        Context.set(new EmptySampleContextObject(url));
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    public void testPathTraversalWithSinglePath() throws Exception {
        setContextAndLifecycle("../opt/");
        assertDoesNotThrow(() -> {
            Paths.get("/opt/");
        });
        Exception exception = assertThrows(RuntimeException.class, () -> {
            Paths.get("/var/../opt/");
        });
        assertEquals("Aikido Zen has blocked Path Traversal",  exception.getMessage());
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    public void testPathTraversalWithMultiplePaths() throws Exception {
        setContextAndLifecycle("../opt/");
        assertDoesNotThrow(() -> {
            Paths.get("/opt/", "../", "var", "../", ".");
        });

        setContextAndLifecycle("../opt/");
        Exception exception = assertThrows(RuntimeException.class, () -> {
            Paths.get("/var/", "/../opt/", ".");
        });

        setContextAndLifecycle("../opt/");
        Exception exception2 = assertThrows(RuntimeException.class, () -> {
            Paths.get("/var/", "/othervar/", "/../opt/", ".");
        });

        assertEquals("Aikido Zen has blocked Path Traversal",  exception.getMessage());
        assertEquals("Aikido Zen has blocked Path Traversal",  exception2.getMessage());
    }
}