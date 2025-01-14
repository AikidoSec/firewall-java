package collectors;

import dev.aikido.agent_api.collectors.FileCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.vulnerabilities.AikidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import utils.EmptySampleContextObject;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class FileCollectorTest {
    public static Path filePath1 = Paths.get("/../../test.txt");
    public static Path filePath2 = Paths.get("/.././../../test.txt");
    public static Path filePath3 = Paths.get("/../../test3.txt");
    public static Path filePath4 = Paths.get("/test.txt");

    @BeforeEach
    public void setup() {
        Context.set(new EmptySampleContextObject("/../../test.txt"));
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    public void testStrings() {
        isPathTraversalAttack("/etc/home/../../test.txt.js");
        isNotPathTraversalAttack("/etc/home/./../test.txt.js");
        isPathTraversalAttack("/etc/home/../../../test.txt.js");
        isNotPathTraversalAttack("/etc/home/../../folder/../test.txt.js");
        isPathTraversalAttack("/../../test.txt");
        isNotPathTraversalAttack("/test.txt");
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    public void testPaths() {
        isPathTraversalAttack(filePath1);
        isPathTraversalAttack(filePath2);

        isNotPathTraversalAttack(filePath3);
        isNotPathTraversalAttack(filePath4);
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    public void testStringArrays() {
        isPathTraversalAttack(new String[]{"/etc/home/../../test.txt.js"});
        isNotPathTraversalAttack(new String[]{"/etc/home/./../test.txt.js", "test.txt.js", "/etc/home"});
        isPathTraversalAttack(new String[]{"unrelated", "/etc/home/../../../test.txt.js", "nottest"});
        isNotPathTraversalAttack(new String[]{"/etc/home/../../folder/../test.txt.js"});
        isPathTraversalAttack(new String[]{"a", "b", "c", "d", "e", "/../../test.txt"});
        isNotPathTraversalAttack(new String[]{"/test.txt"});
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    public void testFileURIs() throws URISyntaxException {
        isPathTraversalAttack(new URI("file:///etc/home/../../test.txt.js"));
        isNotPathTraversalAttack(new URI("file:///etc/home/./../test.txt.js"));
        isPathTraversalAttack("/etc/home/../../../test.txt.js");
        isNotPathTraversalAttack(new URI("file:///etc/home/../../folder/../test.txt.js"));
        isPathTraversalAttack(new URI("file:///../../test.txt"));
        isNotPathTraversalAttack(new URI("http://aikido.dev"));
        isNotPathTraversalAttack(new URI("https://aikido.dev"));
        isNotPathTraversalAttack(new URI("sftp://1.1.1.1"));
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    public void testNotRecognizedObjects() throws MalformedURLException {
        isNotPathTraversalAttack(true);
        isNotPathTraversalAttack(null);
        isNotPathTraversalAttack(Optional.empty());
        isNotPathTraversalAttack(new URL("https://aikido.dev"));
        isNotPathTraversalAttack(new EmptySampleContextObject("/../../test.txt"));
    }


    public void isPathTraversalAttack(Object filePath) {
        Exception exception = assertThrows(AikidoException.class, () -> {
            FileCollector.report(filePath, "testOp");
        });
        assertEquals("Aikido Zen has blocked Path Traversal", exception.getMessage());
    }

    public void isNotPathTraversalAttack(Object filePath) {
        assertDoesNotThrow(() -> {
            FileCollector.report(filePath, "testOp");
        });
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    public void testMaxRecursion() {
        isPathTraversalAttack(new Object[]{new Object[]{"/etc/home/../../test.txt.js"}}); // Depth of 1
        isPathTraversalAttack(
            new Object[]{
                new Object[]{
                    new Object[]{"/etc/home/../../test.txt.ts"}
                }
            }
        ); // Depth of
        isNotPathTraversalAttack(
            new Object[]{
                new Object[]{
                    new Object[]{
                        new Object[]{"/etc/home/../../test.txt.css"}
                    }
                }
            }
        ); // Depth of 3
    }
}
