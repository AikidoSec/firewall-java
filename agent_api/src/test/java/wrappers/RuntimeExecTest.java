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
import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static utils.EmtpyThreadCacheObject.getEmptyThreadCacheObject;

public class RuntimeExecTest {
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
            this.body = "{\"key\":\"value\"}"; // Body as a JSON string
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
        ThreadCache.set(getEmptyThreadCacheObject());
    }
    private void setContextAndLifecycle(String url) {
        Context.set(new SampleContextObject(url));
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    public void testShellInjection() {
        setContextAndLifecycle(" -la");
        Exception exception1 = assertThrows(RuntimeException.class, () -> {
            Runtime.getRuntime().exec("ls -la");
        });
        assertEquals("Aikido Zen has blocked Shell Injection", exception1.getMessage());

        cleanup();
        setContextAndLifecycle("whoami");
        Exception exception2 = assertThrows(RuntimeException.class, () -> {
            Runtime.getRuntime().exec("whoami");
        });
        assertEquals("Aikido Zen has blocked Shell Injection", exception2.getMessage());


        cleanup();
        assertDoesNotThrow(() -> {
            Runtime.getRuntime().exec("whoami && ls -la");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Runtime.getRuntime().exec("");
        });
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    public void testOnlyScansStrings() {
        setContextAndLifecycle("whoami");
        assertDoesNotThrow(() -> {
            Runtime.getRuntime().exec(new String[]{"whoami"});
        });
        assertDoesNotThrow(() -> {
            Runtime.getRuntime().exec(new String[]{"whoami"}, new String[]{"MyEnvironmentVar=1"});
        });

        Exception exception1 = assertThrows(RuntimeException.class, () -> {
            Runtime.getRuntime().exec("whoami", new String[]{"MyEnvironmentVar=1"});
        });
        assertEquals("Aikido Zen has blocked Shell Injection", exception1.getMessage());
    }
}