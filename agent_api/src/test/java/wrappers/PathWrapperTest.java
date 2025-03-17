package wrappers;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.storage.ConfigStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import utils.EmptySampleContextObject;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class PathWrapperTest {
    @AfterEach
    void cleanup() {
        Context.set(null);
    }
    @BeforeEach
    void beforeEach() {
        cleanup();
        ConfigStore.updateBlocking(true);
    }
    private void setContextAndLifecycle(String url) {
        Context.set(new EmptySampleContextObject(url));
    }

    @Test
    public void testPathTraversalInResolve() throws Exception {
        setContextAndLifecycle("../opt/");
        Path myPath = Paths.get("/var/");
        assertDoesNotThrow(() -> {
            myPath.resolve("../etc/");
        });
        Exception exception = assertThrows(RuntimeException.class, () -> {
            myPath.resolve("../opt/");
        });
        assertEquals("Aikido Zen has blocked Path Traversal",  exception.getMessage());
    }

    @Test
    public void testPathTraversalInResolveWithPath() throws Exception {
        Path maliciousPath = Paths.get("/../opt/test.txt");
        Path safePath = Paths.get("/../etc/");

        setContextAndLifecycle("../opt/test.txt");
        Path basePath = Paths.get("/var/");
        assertDoesNotThrow(() -> {
            basePath.resolve(safePath);
        });
        Exception exception = assertThrows(RuntimeException.class, () -> {
            basePath.resolve(maliciousPath);
        });
        assertEquals("Aikido Zen has blocked Path Traversal",  exception.getMessage());
    }

    @Test
    public void testPathTraversalInResolveSibling() throws Exception {
        setContextAndLifecycle("../opt/");
        Path myPath = Paths.get("/var/");
        assertDoesNotThrow(() -> {
            myPath.resolveSibling("../etc/");
        });
        Exception exception = assertThrows(RuntimeException.class, () -> {
            myPath.resolveSibling("../opt/");
        });
        assertEquals("Aikido Zen has blocked Path Traversal",  exception.getMessage());
    }

    @Test
    public void testPathTraversalInResolveSiblingWithPath() throws Exception {
        Path basePath = Paths.get("/var/");
        Path maliciousPath = Paths.get("../opt/");
        Path safePath = Paths.get("../etc/");

        setContextAndLifecycle("../opt");
        assertDoesNotThrow(() -> {
            basePath.resolveSibling(safePath);
        });
        Exception exception = assertThrows(RuntimeException.class, () -> {
            basePath.resolveSibling(maliciousPath);
        });
        assertEquals("Aikido Zen has blocked Path Traversal",  exception.getMessage());
    }

    @Test
    public void testPathTraversalInRelativize() throws Exception {
        Path basePath = Paths.get("/var/opt/");
        Path maliciousPath = Paths.get("myfolder/../test.txt");
        Path safePath = Paths.get("/../etc/");

        setContextAndLifecycle("../test.txt");
        assertDoesNotThrow(() -> {
            basePath.relativize(safePath);
        });
        Exception exception = assertThrows(RuntimeException.class, () -> {
            basePath.relativize(maliciousPath);
        });
        assertEquals("Aikido Zen has blocked Path Traversal",  exception.getMessage());
    }
}