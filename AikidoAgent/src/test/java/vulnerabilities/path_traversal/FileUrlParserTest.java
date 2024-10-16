package vulnerabilities.path_traversal;

import dev.aikido.AikidoAgent.vulnerabilities.path_traversal.FileUrlParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileUrlParserTest {

    @Test
    public void testValidFileUrls() throws Exception {
        assertEquals(
                Path.of("/home/user/test.txt").normalize().toString(),
                FileUrlParser.parseAsFileUrl("file:///home/user/test.txt")
        );
        assertEquals(
                Path.of("/etc/passwd").normalize().toString(),
                FileUrlParser.parseAsFileUrl("file:///etc/passwd")
        );
        assertEquals(
                Path.of("/usr/local/bin/script.sh").normalize().toString(),
                FileUrlParser.parseAsFileUrl("file:///usr/local/bin/script.sh")
        );
    }

    @Test
    public void testRelativePaths() throws Exception {
        assertEquals("/test.txt", FileUrlParser.parseAsFileUrl("test.txt"));
        assertEquals("/test.txt", FileUrlParser.parseAsFileUrl("./test.txt"));
        assertEquals("/test.txt", FileUrlParser.parseAsFileUrl("../test.txt"));
        assertEquals("/folder/test.txt", FileUrlParser.parseAsFileUrl("folder/test.txt"));
        assertEquals("/test.txt", FileUrlParser.parseAsFileUrl("folder/../test.txt"));
    }

    @Test
    public void testEdgeCases() throws Exception {
        assertEquals("/", FileUrlParser.parseAsFileUrl(""));
        assertEquals("/", FileUrlParser.parseAsFileUrl("."));
        assertEquals("/", FileUrlParser.parseAsFileUrl(".."));
        assertEquals("/", FileUrlParser.parseAsFileUrl("/.."));
    }

    @Test
    public void testInvalidFileUrls() throws Exception {
        assertEquals(
                Path.of("/invalid/path").normalize().toString(),
                FileUrlParser.parseAsFileUrl("file:///invalid/path")
        );
        assertEquals(
                Path.of("/invalid/path").normalize().toString(),
                FileUrlParser.parseAsFileUrl("file:///../invalid/path")
        );
    }

    @Test
    public void testWindowsPaths() throws Exception {
        assertEquals(
                "/C:/Users/User/test.txt",
                FileUrlParser.parseAsFileUrl("file:///C:/Users/User/test.txt")
        );
        assertEquals(
                "/C:/Users/test.txt",
                FileUrlParser.parseAsFileUrl("file:///C:/Users/User/../test.txt")
        );
    }
}
