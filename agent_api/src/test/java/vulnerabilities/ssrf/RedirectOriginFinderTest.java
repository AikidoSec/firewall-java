package vulnerabilities.ssrf;

import dev.aikido.agent_api.collectors.RedirectCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.EmptySampleContextObject;

import java.net.MalformedURLException;
import java.net.URL;

import static dev.aikido.agent_api.vulnerabilities.ssrf.RedirectOriginFinder.getRedirectOrigin;
import static org.junit.jupiter.api.Assertions.*;

public class RedirectOriginFinderTest {
    @BeforeEach
    public void setup() {
        ContextObject context = new EmptySampleContextObject();
        Context.set(context);
    }

    @Test
    public void testGetRedirectOrigin() throws MalformedURLException {
        RedirectCollector.report(new URL("https://example.com"), new URL("https://hackers.com"), "test-op");
        assertNotNull(getRedirectOrigin("hackers.com", 443));
        assertEquals("https://example.com", getRedirectOrigin("hackers.com", 443).toString());
    }

    @Test
    public void testGetRedirectOrigin2() throws MalformedURLException {
        RedirectCollector.report(new URL("https://example.com"), new URL("https://example.com/2"), "test-op");
        RedirectCollector.report(new URL("https://example.com/2"), new URL("https://hackers.com/test"), "test-op");
        assertEquals(1, Context.get().getRedirectStartNodes().size());
        assertNotNull(getRedirectOrigin("hackers.com", 443));
        assertEquals("https://example.com", getRedirectOrigin("hackers.com", 443).toString());
    }

    @Test
    public void testGetRedirectNoRedirects() {
        assertNull(getRedirectOrigin("hackers.com", 443));
    }

    @Test
    public void testGetRedirectOriginNotADestination() throws MalformedURLException {
        RedirectCollector.report(new URL("https://example.com"), new URL("https://hackers.com"), "test-op");
        assertNull(getRedirectOrigin("example.com", 443));
    }

    @Test
    public void testGetRedirectOriginNotInRedirects() throws MalformedURLException {
        RedirectCollector.report(new URL("https://example.com"), new URL("https://hackers.com"), "test-op");
        assertNull(getRedirectOrigin("example.com", 443));
    }

    @Test
    public void testGetRedirectOriginMultipleRedirects() throws MalformedURLException {
        RedirectCollector.report(new URL("https://example.com"), new URL("https://example.com/2"), "test-op");
        RedirectCollector.report(new URL("https://example.com/2"), new URL("https://hackers.com/test"), "test-op");
        RedirectCollector.report(new URL("https://hackers.com/test"), new URL("https://another.com"), "test-op");

        assertEquals("https://example.com", getRedirectOrigin("hackers.com", 443).toString());
    }

    @Test
    public void testAvoidsInfiniteLoopsWithUnrelatedCyclicRedirects() throws MalformedURLException {
        RedirectCollector.report(new URL("https://cycle.com/a"), new URL("https://cycle.com/b"), "test-op");
        RedirectCollector.report(new URL("https://cycle.com/b"), new URL("https://cycle.com/c"), "test-op");
        RedirectCollector.report(new URL("https://cycle.com/c"), new URL("https://cycle.com/a"), "test-op"); // Unrelated cycle
        RedirectCollector.report(new URL("https://start.com"), new URL("https://middle.com"), "test-op"); // Relevant redirect
        RedirectCollector.report(new URL("https://middle.com"), new URL("https://end.com"), "test-op"); // Relevant redirect

        assertEquals("https://start.com", getRedirectOrigin("end.com", 443).toString());
    }

    @Test
    public void testHandlesMultipleRequestsWithOverlappingRedirects() throws MalformedURLException {
        RedirectCollector.report(new URL("https://site1.com"), new URL("https://site2.com"), "test-op");
        RedirectCollector.report(new URL("https://site2.com"), new URL("https://site3.com"), "test-op");
        RedirectCollector.report(new URL("https://site3.com"), new URL("https://site1.com"), "test-op"); // Cycle
        RedirectCollector.report(new URL("https://origin.com"), new URL("https://destination.com"), "test-op"); // Relevant redirect

        assertEquals("https://origin.com", getRedirectOrigin("destination.com", 443).toString());
    }

    @Test
    public void testAvoidsInfiniteLoopsWhenCyclesArePartOfTheRedirectChain() throws MalformedURLException {
        RedirectCollector.report(new URL("https://start.com"), new URL("https://loop.com/a"), "test-op");
        RedirectCollector.report(new URL("https://loop.com/a"), new URL("https://loop.com/b"), "test-op");
        RedirectCollector.report(new URL("https://loop.com/b"), new URL("https://loop.com/c"), "test-op");
        RedirectCollector.report(new URL("https://loop.com/c"), new URL("https://loop.com/a"), "test-op"); // Cycle here

        assertEquals("https://start.com", getRedirectOrigin("loop.com", 443).toString());
    }

    @Test
    public void testRedirectsWithQueryParameters() throws MalformedURLException {
        RedirectCollector.report(new URL("https://example.com"), new URL("https://example.com?param=value"), "test-op");

        assertEquals("https://example.com", getRedirectOrigin("example.com", 443).toString());
    }

    @Test
    public void testRedirectsWithFragmentIdentifiers() throws MalformedURLException {
        RedirectCollector.report(new URL("https://example.com"), new URL("https://example.com#section"), "test-op");

        assertEquals("https://example.com", getRedirectOrigin("example.com", 443).toString());
    }

    @Test
    public void testRedirectsWithDifferentProtocols() throws MalformedURLException {
        RedirectCollector.report(new URL("http://example.com"), new URL("https://example.com"), "test-op");

        assertEquals("http://example.com", getRedirectOrigin("example.com", 443).toString());
    }

    @Test
    public void testRedirectsWithDifferentPorts() throws MalformedURLException {
        RedirectCollector.report(new URL("https://example.com"), new URL("https://example.com:8080"), "test-op");

        assertEquals("https://example.com", getRedirectOrigin("example.com", 8080).toString());
    }

    @Test
    public void testRedirectsWithPaths() throws MalformedURLException {
        RedirectCollector.report(new URL("https://example.com"), new URL("https://example.com/home"), "test-op");
        RedirectCollector.report(new URL("https://example.com/home"), new URL("https://example.com/home/welcome"), "test-op");

        assertEquals("https://example.com", getRedirectOrigin("example.com", 443).toString());
    }
    @Test
    public void testMultipleRedirectsToSameDestination() throws MalformedURLException {
        RedirectCollector.report(new URL("https://a.com"), new URL("https://d.com"), "test-op");
        RedirectCollector.report(new URL("https://b.com"), new URL("https://d.com"), "test-op");
        RedirectCollector.report(new URL("https://c.com"), new URL("https://d.com"), "test-op");

        assertEquals("https://a.com", getRedirectOrigin("d.com", 443).toString());
    }

    @Test
    public void testMultipleRedirectPathsToSameUrl() throws MalformedURLException {
        RedirectCollector.report(new URL("https://x.com"), new URL("https://y.com"), "test-op");
        RedirectCollector.report(new URL("https://y.com"), new URL("https://z.com"), "test-op");
        RedirectCollector.report(new URL("https://a.com"), new URL("https://b.com"), "test-op");
        RedirectCollector.report(new URL("https://b.com"), new URL("https://z.com"), "test-op");

        assertEquals("https://x.com", getRedirectOrigin("z.com", 443).toString());
    }

    @Test
    public void testReturnsUndefinedWhenSourceAndDestinationAreSameUrl() throws MalformedURLException {
        RedirectCollector.report(new URL("https://example.com"), new URL("https://example.com"), "test-op");

        assertEquals("https://example.com", getRedirectOrigin("example.com", 443).toString());
    }

    @Test
    public void testHandlesVeryLongRedirectChains() throws MalformedURLException {
        for (int i = 0; i < 100; i++) {
            RedirectCollector.report(
                    new URL("https://example.com/" + i),
                    new URL("https://example.com/" + (i + 1)),
                , "test-op"
            );
        }

        assertEquals("https://example.com/0", getRedirectOrigin("example.com", 443).toString());
    }

    @Test
    public void testHandlesRedirectsWithCyclesLongerThanOneRedirect() throws MalformedURLException {
        RedirectCollector.report(new URL("https://a.com"), new URL("https://b.com"), "test-op");
        RedirectCollector.report(new URL("https://b.com"), new URL("https://c.com"), "test-op");
        RedirectCollector.report(new URL("https://c.com"), new URL("https://a.com"), "test-op"); // Cycle

        assertEquals("https://a.com", getRedirectOrigin("a.com", 443).toString());
    }

    @Test
    public void testHandlesRedirectsWithDifferentQueryParameters() throws MalformedURLException {
        RedirectCollector.report(new URL("https://example.com"), new URL("https://example.com?param=1"), "test-op");
        RedirectCollector.report(new URL("https://example.com?param=1"), new URL("https://example.com?param=2"), "test-op");

        assertEquals("https://example.com", getRedirectOrigin("example.com", 443).toString());
    }
    @Test
    public void testRedirectWithMatchingPort() throws MalformedURLException {
        RedirectCollector.report(new URL("https://example.com"), new URL("https://hackers.com:443"), "test-op");
        assertNotNull(getRedirectOrigin("hackers.com", 443));
        assertEquals("https://example.com", getRedirectOrigin("hackers.com", 443).toString());
    }

    @Test
    public void testRedirectWithNonMatchingPort() throws MalformedURLException {
        RedirectCollector.report(new URL("https://example.com"), new URL("https://hackers.com:442"), "test-op");
        assertNull(getRedirectOrigin("hackers.com", 443));
    }
    @Test
    public void testRedirectWithNonMatchingPort2() throws MalformedURLException {
        RedirectCollector.report(new URL("https://example.com"), new URL("https://hackers.com"), "test-op");
        assertNull(getRedirectOrigin("hackers.com", 442));
    }
}
