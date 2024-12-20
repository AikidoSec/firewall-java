package wrappers;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.storage.routes.Routes;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class PathWrapperTest {
    public static class SampleContextObject extends ContextObject {
        public SampleContextObject(String argument) {
            this.method = "GET";
            this.source = "web";
            this.url = "https://example.com/api/resource";
            this.route = "/api/resource";
            this.remoteAddress = "192.168.1.1";
            this.headers = new HashMap<>();

            this.query = new HashMap<>();
            this.query.put("search", new String[]{"example", "dev.aikido:80"});
            this.query.put("sql1", new String[]{"SELECT * FRO"});
            this.query.put("arg", new String[]{argument});

            this.cookies = new HashMap<>();
            this.body = new HashMap<>();
            this.body.put("test", "{\"key\":\"value\"}"); // Body as a JSON string
        }
    }

    @AfterEach
    void cleanup() {
        Context.set(null);
        ThreadCache.set(null);
    }
    @BeforeEach
    void clearThreadCache() {
        cleanup();
        ThreadCache.set(new ThreadCacheObject(List.of(), Set.of(), Set.of(), new Routes()));
    }
    private void setContextAndLifecycle(String url) {
        Context.set(new SampleContextObject(url));
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
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

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
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

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
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

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
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

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
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