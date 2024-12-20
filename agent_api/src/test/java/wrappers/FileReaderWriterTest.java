package wrappers;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.storage.routes.Routes;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FileReaderWriterTest {
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
        String prop = System.getProperty("AIK_INTERNAL_coverage_run");
        Assumptions.assumeFalse(prop != null && prop.equals("1"), "With coverage enabled we skip File(...) test cases.");
    }
    private void setContextAndLifecycle(String url) {
        Context.set(new SampleContextObject(url));
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