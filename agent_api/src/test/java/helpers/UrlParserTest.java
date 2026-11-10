package helpers;

import dev.aikido.agent_api.helpers.url.UrlParser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    public void testTryParseUrlPathKeepsDotSegments() {
        // Routers match dot segments literally, resolving them is a separate step (resolveDotSegments).
        assertEquals("/public/../admin/delete", UrlParser.tryParseUrlPath("/public/../admin/delete"));
        assertEquals("/public/../admin/delete", UrlParser.tryParseUrlPath("http://localhost/public/../admin/delete"));
        assertEquals("/public/../admin/delete", UrlParser.tryParseUrlPath("/public/%2e%2e/admin/delete"));
        assertEquals("/public/../admin/delete", UrlParser.tryParseUrlPath("/public/.%2E/admin/delete"));
        assertEquals("/./admin/delete", UrlParser.tryParseUrlPath("/%2e/admin/delete"));
    }

    @Test
    public void testTryParseUrlPathDecodesPercentEncoding() {
        assertEquals("/admin/delete", UrlParser.tryParseUrlPath("/%61dmin/delete"));
        assertEquals("/posts/caf\u00e9", UrlParser.tryParseUrlPath("/posts/caf%C3%A9"));
        assertEquals("/posts/100%", UrlParser.tryParseUrlPath("/posts/100%25"));
        assertEquals("/posts/a+b", UrlParser.tryParseUrlPath("/posts/a+b"));
        assertEquals("/posts/a+b", UrlParser.tryParseUrlPath("/posts/a%2Bb"));
        assertEquals("/posts/a/b", UrlParser.tryParseUrlPath("/posts/a%2Fb"));
    }

    @Test
    public void testTryParseUrlPathMergesDuplicateSlashes() {
        assertEquals("/admin/delete", UrlParser.tryParseUrlPath("//admin//delete"));
        assertEquals("/admin/delete", UrlParser.tryParseUrlPath("http://localhost/admin///delete"));
        assertEquals("/admin/delete", UrlParser.tryParseUrlPath("/admin/%2F/delete"));
    }

    @Test
    public void testTryParseUrlPathStripsPathParameters() {
        assertEquals("/admin/delete", UrlParser.tryParseUrlPath("/admin;jsessionid=abc/delete"));
        assertEquals("/admin/delete", UrlParser.tryParseUrlPath("/admin/delete;x=y"));
        assertEquals("/public/../admin/delete", UrlParser.tryParseUrlPath("/public/..;/admin/delete"));
        // An encoded semicolon is part of the segment, not the start of a path parameter:
        assertEquals("/admin/a;b", UrlParser.tryParseUrlPath("/admin/a%3Bb"));
    }

    @Test
    public void testTryParseUrlPathKeepsTrailingSlash() {
        assertEquals("/posts/", UrlParser.tryParseUrlPath("/posts/"));
        assertEquals("/posts/", UrlParser.tryParseUrlPath("http://localhost:8080/posts/?a=b"));
    }

    @Test
    public void testTryParseUrlPathForUrlsRejectedByJavaNetUri() {
        // Servers still route these, so we need a path for them.
        assertEquals("/admin/delete", UrlParser.tryParseUrlPath("http://localhost/admin/delete?q=%zz"));
        assertEquals("/admin/delete", UrlParser.tryParseUrlPath("http://localhost/admin/delete?q=a|b"));
        assertEquals("/admin/delete", UrlParser.tryParseUrlPath("/admin/delete?q={x}#\"y\""));
        assertEquals("/files/a|b", UrlParser.tryParseUrlPath("http://localhost/files/a|b"));
        assertEquals("/files/{x}", UrlParser.tryParseUrlPath("http://localhost/files/{x}"));
        assertEquals("/files/a b", UrlParser.tryParseUrlPath("http://localhost/files/a b"));
    }

    @Test
    public void testTryParseUrlPathKeepsMalformedEscapes() {
        assertEquals("/files/%zz", UrlParser.tryParseUrlPath("/files/%zz"));
        assertEquals("/files/a%2", UrlParser.tryParseUrlPath("/files/a%2"));
        assertEquals("/admin/%", UrlParser.tryParseUrlPath("/%61dmin/%"));
    }

    @Test
    public void testTryParseUrlPathIgnoresTheAuthority() {
        assertEquals("/posts", UrlParser.tryParseUrlPath("http://user:pass@localhost:8080/posts"));
        assertEquals("/posts", UrlParser.tryParseUrlPath("http://[::1]:8080/posts"));
        assertEquals("/", UrlParser.tryParseUrlPath("http://localhost:8080"));
        assertNull(UrlParser.tryParseUrlPath(null));
        assertNull(UrlParser.tryParseUrlPath(""));
        assertNull(UrlParser.tryParseUrlPath("localhost/posts"));
    }

    @Test
    public void testResolveDotSegments() {
        assertEquals("/admin/delete", UrlParser.resolveDotSegments("/public/../admin/delete"));
        assertEquals("/admin/delete", UrlParser.resolveDotSegments("/a/b/../../admin/delete"));
        assertEquals("/admin/delete", UrlParser.resolveDotSegments("/./admin/./delete"));
        assertEquals("/admin/delete", UrlParser.resolveDotSegments("/../../admin/delete"));
        assertEquals("/admin/", UrlParser.resolveDotSegments("/public/../admin/"));
        assertEquals("/admin/", UrlParser.resolveDotSegments("/admin/delete/.."));
        assertEquals("/admin/", UrlParser.resolveDotSegments("/admin/."));
        assertEquals("/", UrlParser.resolveDotSegments("/admin/.."));
        assertEquals("/", UrlParser.resolveDotSegments("/"));
        assertEquals("/admin/delete", UrlParser.resolveDotSegments("/admin/delete"));
        // Only whole segments count:
        assertEquals("/admin/..delete/...", UrlParser.resolveDotSegments("/admin/..delete/..."));
    }

    @Test
    public void testHasDotSegments() {
        assertTrue(UrlParser.hasDotSegments("/public/../admin"));
        assertTrue(UrlParser.hasDotSegments("/admin/."));
        assertTrue(UrlParser.hasDotSegments("/.."));
        assertFalse(UrlParser.hasDotSegments("/"));
        assertFalse(UrlParser.hasDotSegments("/admin/delete/"));
        assertFalse(UrlParser.hasDotSegments("/admin/..delete/.../file.txt"));
    }
}
