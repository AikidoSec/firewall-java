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
        RedirectCollector.report(new URL("https://example.com"), new URL("https://hackers.com"));
        assertNotNull(getRedirectOrigin("hackers.com", 443));
        assertEquals("https://example.com", getRedirectOrigin("hackers.com", 443).toString());
    }

    @Test
    public void testGetRedirectOrigin2() throws MalformedURLException {
        RedirectCollector.report(new URL("https://example.com"), new URL("https://example.com/2"));
        RedirectCollector.report(new URL("https://example.com/2"), new URL("https://hackers.com/test"));
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
        RedirectCollector.report(new URL("https://example.com"), new URL("https://hackers.com"));
        assertNull(getRedirectOrigin("example.com", 443));
    }

    @Test
    public void testGetRedirectOriginNotInRedirects() throws MalformedURLException {
        RedirectCollector.report(new URL("https://example.com"), new URL("https://hackers.com"));
        assertNull(getRedirectOrigin("example.com", 443));
    }

    @Test
    public void testGetRedirectOriginMultipleRedirects() throws MalformedURLException {
        RedirectCollector.report(new URL("https://example.com"), new URL("https://example.com/2"));
        RedirectCollector.report(new URL("https://example.com/2"), new URL("https://hackers.com/test"));
        RedirectCollector.report(new URL("https://hackers.com/test"), new URL("https://another.com"));

        assertEquals("https://example.com", getRedirectOrigin("hackers.com", 443).toString());
    }

    @Test
    public void testAvoidsInfiniteLoopsWithUnrelatedCyclicRedirects() throws MalformedURLException {
        RedirectCollector.report(new URL("https://cycle.com/a"), new URL("https://cycle.com/b"));
        RedirectCollector.report(new URL("https://cycle.com/b"), new URL("https://cycle.com/c"));
        RedirectCollector.report(new URL("https://cycle.com/c"), new URL("https://cycle.com/a")); // Unrelated cycle
        RedirectCollector.report(new URL("https://start.com"), new URL("https://middle.com")); // Relevant redirect
        RedirectCollector.report(new URL("https://middle.com"), new URL("https://end.com")); // Relevant redirect

        assertEquals("https://start.com", getRedirectOrigin("end.com", 443).toString());
    }

    @Test
    public void testHandlesMultipleRequestsWithOverlappingRedirects() throws MalformedURLException {
        RedirectCollector.report(new URL("https://site1.com"), new URL("https://site2.com"));
        RedirectCollector.report(new URL("https://site2.com"), new URL("https://site3.com"));
        RedirectCollector.report(new URL("https://site3.com"), new URL("https://site1.com")); // Cycle
        RedirectCollector.report(new URL("https://origin.com"), new URL("https://destination.com")); // Relevant redirect

        assertEquals("https://origin.com", getRedirectOrigin("destination.com", 443).toString());
    }

    @Test
    public void testAvoidsInfiniteLoopsWhenCyclesArePartOfTheRedirectChain() throws MalformedURLException {
        RedirectCollector.report(new URL("https://start.com"), new URL("https://loop.com/a"));
        RedirectCollector.report(new URL("https://loop.com/a"), new URL("https://loop.com/b"));
        RedirectCollector.report(new URL("https://loop.com/b"), new URL("https://loop.com/c"));
        RedirectCollector.report(new URL("https://loop.com/c"), new URL("https://loop.com/a")); // Cycle here

        assertEquals("https://start.com", getRedirectOrigin("loop.com", 443).toString());
    }

    @Test
    public void testRedirectsWithQueryParameters() throws MalformedURLException {
        RedirectCollector.report(new URL("https://example.com"), new URL("https://example.com?param=value"));

        assertEquals("https://example.com", getRedirectOrigin("example.com", 443).toString());
    }

    @Test
    public void testRedirectsWithFragmentIdentifiers() throws MalformedURLException {
        RedirectCollector.report(new URL("https://example.com"), new URL("https://example.com#section"));

        assertEquals("https://example.com", getRedirectOrigin("example.com", 443).toString());
    }

    @Test
    public void testRedirectsWithDifferentProtocols() throws MalformedURLException {
        RedirectCollector.report(new URL("http://example.com"), new URL("https://example.com"));

        assertEquals("http://example.com", getRedirectOrigin("example.com", 443).toString());
    }

    @Test
    public void testRedirectsWithDifferentPorts() throws MalformedURLException {
        RedirectCollector.report(new URL("https://example.com"), new URL("https://example.com:8080"));

        assertEquals("https://example.com", getRedirectOrigin("example.com", 8080).toString());
    }

    @Test
    public void testRedirectsWithPaths() throws MalformedURLException {
        RedirectCollector.report(new URL("https://example.com"), new URL("https://example.com/home"));
        RedirectCollector.report(new URL("https://example.com/home"), new URL("https://example.com/home/welcome"));

        assertEquals("https://example.com", getRedirectOrigin("example.com", 443).toString());
    }

    @Test
    public void testMultipleRedirectsToSameDestination() throws MalformedURLException {
        RedirectCollector.report(new URL("https://a.com"), new URL("https://d.com"));
        RedirectCollector.report(new URL("https://b.com"), new URL("https://d.com"));
        RedirectCollector.report(new URL("https://c.com"), new URL("https://d.com"));

        assertEquals("https://a.com", getRedirectOrigin("d.com", 443).toString());
    }

    @Test
    public void testMultipleRedirectPathsToSameUrl() throws MalformedURLException {
        RedirectCollector.report(new URL("https://x.com"), new URL("https://y.com"));
        RedirectCollector.report(new URL("https://y.com"), new URL("https://z.com"));
        RedirectCollector.report(new URL("https://a.com"), new URL("https://b.com"));
        RedirectCollector.report(new URL("https://b.com"), new URL("https://z.com"));

        assertEquals("https://x.com", getRedirectOrigin("z.com", 443).toString());
    }

    @Test
    public void testReturnsUndefinedWhenSourceAndDestinationAreSameUrl() throws MalformedURLException {
        RedirectCollector.report(new URL("https://example.com"), new URL("https://example.com"));

        assertEquals("https://example.com", getRedirectOrigin("example.com", 443).toString());
    }

    @Test
    public void testHandlesVeryLongRedirectChains() throws MalformedURLException {
        for (int i = 0; i < 100; i++) {
            RedirectCollector.report(
                new URL("https://example.com/" + i),
                new URL("https://example.com/" + (i + 1))
            );
        }

        assertEquals("https://example.com/0", getRedirectOrigin("example.com", 443).toString());
    }

    @Test
    public void testHandlesRedirectsWithCyclesLongerThanOneRedirect() throws MalformedURLException {
        RedirectCollector.report(new URL("https://a.com"), new URL("https://b.com"));
        RedirectCollector.report(new URL("https://b.com"), new URL("https://c.com"));
        RedirectCollector.report(new URL("https://c.com"), new URL("https://a.com")); // Cycle

        assertEquals("https://a.com", getRedirectOrigin("a.com", 443).toString());
    }

    @Test
    public void testHandlesRedirectsWithDifferentQueryParameters() throws MalformedURLException {
        RedirectCollector.report(new URL("https://example.com"), new URL("https://example.com?param=1"));
        RedirectCollector.report(new URL("https://example.com?param=1"), new URL("https://example.com?param=2"));

        assertEquals("https://example.com", getRedirectOrigin("example.com", 443).toString());
    }

    @Test
    public void testRedirectWithMatchingPort() throws MalformedURLException {
        RedirectCollector.report(new URL("https://example.com"), new URL("https://hackers.com:443"));
        assertNotNull(getRedirectOrigin("hackers.com", 443));
        assertEquals("https://example.com", getRedirectOrigin("hackers.com", 443).toString());
    }

    @Test
    public void testRedirectWithNonMatchingPort() throws MalformedURLException {
        RedirectCollector.report(new URL("https://example.com"), new URL("https://hackers.com:442"));
        assertNull(getRedirectOrigin("hackers.com", 443));
    }

    @Test
    public void testRedirectWithNonMatchingPort2() throws MalformedURLException {
        RedirectCollector.report(new URL("https://example.com"), new URL("https://hackers.com"));
        assertNull(getRedirectOrigin("hackers.com", 442));
    }
}
