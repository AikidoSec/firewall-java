package helpers;

import dev.aikido.agent_api.helpers.url.UrlParser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UrlParserTest {

    @Test
    public void testTryParseUrlPathNothingFound() {
        assertNull(UrlParser.tryParseUrlPath("abc"));
    }

    @Test
    public void testTryParseUrlPathForRoot() {
        assertEquals("/", UrlParser.tryParseUrlPath("/"));
    }

    @Test
    public void testTryParseUrlPathForRelativeUrl() {
        assertEquals("/posts", UrlParser.tryParseUrlPath("/posts"));
    }

    @Test
    public void testTryParseUrlPathForRelativeUrlWithQuery() {
        assertEquals("/posts", UrlParser.tryParseUrlPath("/posts?abc=def"));
    }

    @Test
    public void testTryParseUrlPathForAbsoluteUrl() {
        assertEquals("/posts/3", UrlParser.tryParseUrlPath("http://localhost/posts/3"));
    }

    @Test
    public void testTryParseUrlPathForAbsoluteUrlWithQuery() {
        assertEquals("/posts/3", UrlParser.tryParseUrlPath("http://localhost/posts/3?abc=def"));
    }

    @Test
    public void testTryParseUrlPathForAbsoluteUrlWithHash() {
        assertEquals("/posts/3", UrlParser.tryParseUrlPath("http://localhost/posts/3#abc"));
    }

    @Test
    public void testTryParseUrlPathForAbsoluteUrlWithQueryAndHash() {
        assertEquals("/posts/3", UrlParser.tryParseUrlPath("http://localhost/posts/3?abc=def#ghi"));
    }

    @Test
    public void testTryParseUrlPathForAbsoluteUrlWithQueryAndHashNoPath() {
        assertEquals("/", UrlParser.tryParseUrlPath("http://localhost/?abc=def#ghi"));
    }

    @Test
    public void testTryParseUrlPathForAbsoluteUrlWithQueryNoPath() {
        assertEquals("/", UrlParser.tryParseUrlPath("http://localhost?abc=def"));
    }

    @Test
    public void testTryParseUrlPathForAbsoluteUrlWithHashNoPath() {
        assertEquals("/", UrlParser.tryParseUrlPath("http://localhost#abc"));
    }
}
