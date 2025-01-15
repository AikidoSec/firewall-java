package wrappers;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import utils.EmptySampleContextObject;

import static org.junit.jupiter.api.Assertions.*;
import static utils.EmtpyThreadCacheObject.getEmptyThreadCacheObject;

public class RuntimeExecTest {
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