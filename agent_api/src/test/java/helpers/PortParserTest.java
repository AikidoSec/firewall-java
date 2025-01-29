package helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.aikido.agent_api.helpers.url.PortParser;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.Test;

public class PortParserTest {

    @Test
    public void testGetPortFromURL_WithSpecifiedPort() throws MalformedURLException {
        URL url = new URL("http://example.com:8080");
        int port = PortParser.getPortFromURL(url);
        assertEquals(8080, port);
    }

    @Test
    public void testGetPortFromURL_WithHttpProtocol() throws MalformedURLException {
        URL url = new URL("http://example.com");
        int port = PortParser.getPortFromURL(url);
        assertEquals(80, port);
    }

    @Test
    public void testGetPortFromURL_WithHttpsProtocol() throws MalformedURLException {
        URL url = new URL("https://example.com");
        int port = PortParser.getPortFromURL(url);
        assertEquals(443, port);
    }

    @Test
    public void testGetPortFromURL_WithDifferentProtocol() throws MalformedURLException {
        URL url = new URL("ftp://example.com");
        int port = PortParser.getPortFromURL(url);
        assertEquals(-1, port);
    }
}
