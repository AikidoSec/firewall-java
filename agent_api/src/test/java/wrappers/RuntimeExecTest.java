package wrappers;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.EmptySampleContextObject;

import static org.junit.jupiter.api.Assertions.*;

public class RuntimeExecTest {
    @AfterEach
    void cleanup() {
        Context.set(null);
    }
    @BeforeEach
    void beforeEach() {
        cleanup();
        ServiceConfigStore.updateBlocking(true);
    }
    private void setContextAndLifecycle(String url) {
        Context.set(new EmptySampleContextObject(url));
    }

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