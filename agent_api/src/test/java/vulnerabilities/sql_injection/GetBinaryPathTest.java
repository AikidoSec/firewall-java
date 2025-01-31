package vulnerabilities.sql_injection;

import dev.aikido.agent_api.vulnerabilities.sql_injection.GetBinaryPath;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class GetBinaryPathTest {
    @BeforeAll
    public static void copyStart() {
        System.setProperty("copy.AIK_agent_dir", System.getProperty("AIK_agent_dir"));
        System.setProperty("copy.os.name", System.getProperty("os.name"));
        System.setProperty("copy.os.arch", System.getProperty("os.arch"));
    }
    @AfterAll
    public static void cleanup() {
        System.setProperty("AIK_agent_dir", System.getProperty("copy.AIK_agent_dir"));
        System.setProperty("os.name", System.getProperty("copy.os.name"));
        System.setProperty("os.arch", System.getProperty("copy.os.arch"));
    }

    @BeforeEach
    public void setUp() {
        // Clear system properties before each test
        System.clearProperty("AIK_agent_dir");
        System.clearProperty("os.name");
        System.clearProperty("os.arch");
    }

    @Test
    public void testGetPathForBinary_WithWindows64() {
        System.setProperty("AIK_agent_dir", "/path/to/agent");
        System.setProperty("os.name", "Windows 10");
        System.setProperty("os.arch", "amd64");

        String expectedPath = "/path/to/agent/binaries/libzen_internals_x86_64-pc-windows-gnu.dll";
        String actualPath = GetBinaryPath.getPathForBinary();
        assertEquals(expectedPath, actualPath);
    }

    @Test
    public void testGetPathForBinary_WithWindowsARM64() {
        System.setProperty("AIK_agent_dir", "/path/to/agent");
        System.setProperty("os.name", "Windows 10");
        System.setProperty("os.arch", "aarch64");

        String expectedPath = "/path/to/agent/binaries/libzen_internals_aarch64-pc-windows-gnu.dll";
        String actualPath = GetBinaryPath.getPathForBinary();
        assertEquals(expectedPath, actualPath);
    }

    @Test
    public void testGetPathForBinary_WithMac() {
        System.setProperty("AIK_agent_dir", "/path/to/agent");
        System.setProperty("os.name", "Mac OS X");
        System.setProperty("os.arch", "x86_64");

        String expectedPath = "/path/to/agent/binaries/libzen_internals_x86_64-apple-darwin.dylib";
        String actualPath = GetBinaryPath.getPathForBinary();
        assertEquals(expectedPath, actualPath);
    }

    @Test
    public void testGetPathForBinary_WithLinux() {
        System.setProperty("AIK_agent_dir", "/path/to/agent");
        System.setProperty("os.name", "Linux");
        System.setProperty("os.arch", "x86_64");

        String expectedPath = "/path/to/agent/binaries/libzen_internals_x86_64-unknown-linux-gnu.so";
        String actualPath = GetBinaryPath.getPathForBinary();
        assertEquals(expectedPath, actualPath);
    }

    @Test
    public void testGetPathForBinary_WithLinuxARM64() {
        System.setProperty("AIK_agent_dir", "/path/to/agent");
        System.setProperty("os.name", "Linux");
        System.setProperty("os.arch", "aarch64");

        String expectedPath = "/path/to/agent/binaries/libzen_internals_aarch64-unknown-linux-gnu.so";
        String actualPath = GetBinaryPath.getPathForBinary();
        assertEquals(expectedPath, actualPath);
    }

    @Test
    public void testGetPathForBinary_WithNoAgentDir() {
        System.setProperty("os.name", "Linux");
        System.setProperty("os.arch", "x86_64");

        String actualPath = GetBinaryPath.getPathForBinary();
        assertNull(actualPath);
    }

    @Test
    public void testGetPathForBinary_WithEmptyAgentDir() {
        System.setProperty("AIK_agent_dir", "");
        System.setProperty("os.name", "Linux");
        System.setProperty("os.arch", "x86_64");

        String actualPath = GetBinaryPath.getPathForBinary();
        assertEquals("/binaries/libzen_internals_x86_64-unknown-linux-gnu.so", actualPath);
    }
    @Test
    public void testGetPathForBinary_WithUnknownOS() {
        System.setProperty("AIK_agent_dir", "/path/to/agent");
        System.setProperty("os.name", "Unknown OS");
        System.setProperty("os.arch", "x86_64");

        String expectedPath = "/path/to/agent/binaries/libzen_internals_x86_64-unknown-linux-gnu.so";
        String actualPath = GetBinaryPath.getPathForBinary();
        assertEquals(expectedPath, actualPath);
    }
    @Test
    public void testGetPathForBinary_WithUnknownArchitecture() {
        System.setProperty("AIK_agent_dir", "/path/to/agent");
        System.setProperty("os.name", "Linux");
        System.setProperty("os.arch", "unknown-arch");

        String expectedPath = "/path/to/agent/binaries/libzen_internals_x86_64-unknown-linux-gnu.so";
        String actualPath = GetBinaryPath.getPathForBinary();
        assertEquals(expectedPath, actualPath);
    }
}