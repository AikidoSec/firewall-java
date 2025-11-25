package wrappers;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.EmptySampleContextObject;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ProcessBuilderTest {
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
            new ProcessBuilder("yjytjyjty", "-c", "ls -la").start();
        });
        assertEquals("Aikido Zen has blocked Shell Injection", exception1.getMessage());

        cleanup();
        setContextAndLifecycle("whoami");
        Exception exception2 = assertThrows(RuntimeException.class, () -> {
            new ProcessBuilder("bash", "-c", "whoami").start();
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

    // --- NEW TEST CASES ---

    @Test
    public void testProcessBuilderCommandModification() {
        setContextAndLifecycle("whoami");
        ProcessBuilder builder = new ProcessBuilder();
        assertDoesNotThrow(() -> {
            builder.command("whoami");
            builder.start();
        });

        Exception exception = assertThrows(RuntimeException.class, () -> {
            builder.command("sh", "-c", "whoami");
            builder.start();
        });
        assertEquals("Aikido Zen has blocked Shell Injection", exception.getMessage());
    }

    @Test
    public void testProcessBuilderWithDifferentShells() {
        setContextAndLifecycle("whoami");
        Exception shException = assertThrows(RuntimeException.class, () -> {
            new ProcessBuilder("sh", "-c", "whoami").start();
        });
        assertEquals("Aikido Zen has blocked Shell Injection", shException.getMessage());

        Exception bashException = assertThrows(RuntimeException.class, () -> {
            new ProcessBuilder("bash", "-c", "whoami").start();
        });
        assertEquals("Aikido Zen has blocked Shell Injection", bashException.getMessage());

        Exception zshException = assertThrows(RuntimeException.class, () -> {
            new ProcessBuilder("zsh", "-c", "whoami").start();
        });
        assertEquals("Aikido Zen has blocked Shell Injection", zshException.getMessage());
    }

    @Test
    public void testProcessBuilderWithDirectCommand() {
        setContextAndLifecycle("whoami");
        assertDoesNotThrow(() -> {
            new ProcessBuilder("whoami").start();
        });
    }

    @Test
    public void testProcessBuilderWithArguments() {
        setContextAndLifecycle("whoami");
        assertDoesNotThrow(() -> {
            new ProcessBuilder("ls", "-l", "/tmp").start();
        });
    }

    @Test
    public void testProcessBuilderWithEnvironment() {
        setContextAndLifecycle("whoami");
        ProcessBuilder builder = new ProcessBuilder("whoami");
        builder.environment().put("MY_VAR", "1");
        assertDoesNotThrow(() -> {
            builder.start();
        });
    }

    @Test
    public void testProcessBuilderWithShellInjectionInCommand() {
        setContextAndLifecycle("whoami; ls");
        Exception exception = assertThrows(RuntimeException.class, () -> {
            new ProcessBuilder("sh", "-c", "whoami; ls").start();
        });
        assertEquals("Aikido Zen has blocked Shell Injection", exception.getMessage());
    }

    @Test
    public void testProcessBuilderWithComplexShellCommand() {
        setContextAndLifecycle("whoami && ls -la");
        Exception exception = assertThrows(RuntimeException.class, () -> {
            new ProcessBuilder("bash", "-c", "whoami && ls -la").start();
        });
        assertEquals("Aikido Zen has blocked Shell Injection", exception.getMessage());
    }

    @Test
    public void testProcessBuilderWithSafeCommand() {
        setContextAndLifecycle("whoami");
        assertDoesNotThrow(() -> {
            new ProcessBuilder("whoami").start();
        });
    }

    @Test
    public void testProcessBuilderWithEmptyCommand() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            new ProcessBuilder().start();
        });
    }

    @Test
    public void testProcessBuilderWithNullCommand() {
        assertThrows(NullPointerException.class, () -> {
            new ProcessBuilder((String[]) null).start();
        });
    }

    @Test
    public void testProcessBuilderWithCommandModificationAfterStart() {
        setContextAndLifecycle("whoami");
        ProcessBuilder builder = new ProcessBuilder("whoami");
        assertDoesNotThrow(() -> {
            builder.start();
        });
        // Modifying command after start should not affect previous process
        builder.command("sh", "-c", "whoami");
        Exception exception = assertThrows(RuntimeException.class, () -> {
            builder.start();
        });
        assertEquals("Aikido Zen has blocked Shell Injection", exception.getMessage());
    }
}
