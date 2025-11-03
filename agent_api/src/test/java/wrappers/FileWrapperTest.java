package wrappers;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.EmptySampleContextObject;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FileWrapperTest {
    @AfterEach
    void cleanup() {
        Context.set(null);
    }
    @BeforeEach
    void beforeEach() {
        cleanup();
        String prop = System.getProperty("AIK_INTERNAL_coverage_run");
        Assumptions.assumeFalse(prop != null && prop.equals("1"), "With coverage enabled we skip File(...) test cases.");
        ServiceConfigStore.updateBlocking(true);
    }
    private void setContextAndLifecycle(String url) {
        Context.set(new EmptySampleContextObject(url));
    }

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

    @Test
    public void testOsCreatePathWithMultipleSlashes() {
        String filePath = "/////etc/passwd";
        setContextAndLifecycle(filePath);
        assertThrows(RuntimeException.class, () -> {
            Path fullPath = Paths.get(filePath);
            List<String> lines = Files.readAllLines(fullPath);
            System.err.println(lines);
        });
    }

    @Test
    public void testOsCreatePathWithMultipleSlashesNegative() {
        String filePath = "safe/relative/path";
        setContextAndLifecycle(filePath);
        assertDoesNotThrow(() -> {
            File fullPath = new File("flaskr/resources/blogs/", filePath);
            fullPath.exists(); // Simulate access
        });
    }

    @Test
    public void testOsCreatePathWithMultipleDoubleSlashes() {
        String filePath = "////etc//passwd";
        setContextAndLifecycle(filePath);
        assertThrows(RuntimeException.class, () -> {
            File fullPath = new File("flaskr/resources/blogs/", filePath);
            fullPath.exists(); // Simulate access
        });
    }

    @Test
    public void testOsCreatePathWithMultipleDoubleSlashesNegative() {
        String filePath = "safe//relative//path";
        setContextAndLifecycle(filePath);
        assertDoesNotThrow(() -> {
            File fullPath = new File("flaskr/resources/blogs/", filePath);
            fullPath.exists(); // Simulate access
        });
    }

    @Test
    public void testOsPathTraversalWithMultipleSlashes() {
        String filePath = "home///..////..////my_secret.txt";
        setContextAndLifecycle(filePath);
        assertThrows(RuntimeException.class, () -> {
            File fullPath = new File("flaskr/resources/blogs/", filePath);
            fullPath.exists(); // Simulate access
        });
    }
}
