package wrappers;

import dev.aikido.agent_api.context.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import utils.EmptySampleContextObject;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class PathsWrapperTest {
    @AfterEach
    void cleanup() {
        Context.set(null);
    }
    @BeforeEach
    void clearThreadCache() {
        cleanup();
    }
    private void setContextAndLifecycle(String url) {
        Context.set(new EmptySampleContextObject(url));
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCK", value = "true")
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
    @SetEnvironmentVariable(key = "AIKIDO_BLOCK", value = "true")
    @Test
    public void testPathTraversalWithMultiplePaths() throws Exception {
        setContextAndLifecycle("../opt/");
        assertDoesNotThrow(() -> {
            Paths.get("/opt/", "../", "var", "../", ".");
        });
        Exception exception = assertThrows(RuntimeException.class, () -> {
            Paths.get("/var/", "/../opt/", ".");
        });
        Exception exception2 = assertThrows(RuntimeException.class, () -> {
            Paths.get("/var/", "/othervar/", "/../opt/", ".");
        });
        assertEquals("Aikido Zen has blocked Path Traversal",  exception.getMessage());
        assertEquals("Aikido Zen has blocked Path Traversal",  exception2.getMessage());

    }
}