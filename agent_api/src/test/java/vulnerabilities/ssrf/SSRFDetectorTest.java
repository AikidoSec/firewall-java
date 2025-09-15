package vulnerabilities.ssrf;

import dev.aikido.agent_api.collectors.RedirectCollector;
import dev.aikido.agent_api.collectors.URLCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import dev.aikido.agent_api.vulnerabilities.Attack;
import dev.aikido.agent_api.vulnerabilities.ssrf.SSRFDetector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import utils.EmptySampleContextObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static utils.EmptyAPIResponses.emptyAPIResponse;

public class SSRFDetectorTest {
    @BeforeAll
    static void cleanup() {
        Context.set(null);
        ServiceConfigStore.updateFromAPIResponse(emptyAPIResponse);
    }
    @AfterAll
    static void afterAll() {
        cleanup();
    }

    private void setContextAndLifecycle(String url) {
        Context.set(new EmptySampleContextObject(url));
        ServiceConfigStore.updateFromAPIResponse(emptyAPIResponse);
    }


    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token")
    public void testSsrfDetectorWithRedirectTo127IP() throws MalformedURLException {
        // Setup context :
        setContextAndLifecycle("http://ssrf-redirects.testssandbox.com/ssrf-test");

        URLCollector.report(new URL("http://ssrf-redirects.testssandbox.com/ssrf-test"));
        RedirectCollector.report(new URL("http://ssrf-redirects.testssandbox.com/ssrf-test"), new URL("http://127.0.0.1:8080"));
        Attack attackData = new SSRFDetector().run(
                "127.0.0.1", 8080,
                List.of("127.0.0.1"),
                "testop"
        );

        assertNotNull(attackData);
        assertEquals("testop", attackData.operation);
        assertEquals("ssrf", attackData.kind);
        assertEquals("query", attackData.source);
        assertEquals("http://ssrf-redirects.testssandbox.com/ssrf-test", attackData.payload);
        assertEquals(".arg.[0]", attackData.pathToPayload);
        assertEquals("127.0.0.1", attackData.metadata.get("hostname"));
        assertEquals("8080", attackData.metadata.get("port"));
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token")
    public void testSsrfDetectorWithRedirectTo127IPButHostnameCapitalizationDifferent() throws MalformedURLException {
        // Setup context :
        setContextAndLifecycle("http://Ssrf-redirects.testssandbox.com/ssrf-test");

        URLCollector.report(new URL("http://Ssrf-redirects.testssandbox.com/ssrf-test"));
        RedirectCollector.report(new URL("http://ssrf-Redirects.testssandbox.com/ssrf-test"), new URL("http://127.0.0.1:8080"));
        Attack attackData = new SSRFDetector().run(
            "127.0.0.1", 8080,
            List.of("127.0.0.1"),
            "testop"
        );

        assertNotNull(attackData);
        assertEquals("testop", attackData.operation);
        assertEquals("ssrf", attackData.kind);
        assertEquals("query", attackData.source);
        assertEquals("http://Ssrf-redirects.testssandbox.com/ssrf-test", attackData.payload);
        assertEquals(".arg.[0]", attackData.pathToPayload);
        assertEquals("127.0.0.1", attackData.metadata.get("hostname"));
        assertEquals("8080", attackData.metadata.get("port"));
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token")
    public void testSsrfDetectorWithRedirectToLocalhost() throws MalformedURLException {
        // Setup context :
        setContextAndLifecycle("http://ssrf-redirects.testssandbox.com/");

        URLCollector.report(new URL("http://ssrf-redirects.testssandbox.com/ssrf-test"));
        RedirectCollector.report(new URL("http://ssrf-redirects.testssandbox.com/ssrf-test"), new URL("http://localhost"));
        Attack attackData = new SSRFDetector().run(
                "localhost", 80,
                List.of("127.0.0.1"),
                "test2nd_op"
        );

        assertNotNull(attackData);
        assertEquals("test2nd_op", attackData.operation);
        assertEquals("ssrf", attackData.kind);
        assertEquals("query", attackData.source);
        assertEquals("http://ssrf-redirects.testssandbox.com/", attackData.payload);
        assertEquals(".arg.[0]", attackData.pathToPayload);
        assertEquals("localhost", attackData.metadata.get("hostname"));
        assertEquals("80", attackData.metadata.get("port"));
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token")
    public void testSsrfDetectorWithRedirectToLocalhostButIsRequestToItself() throws MalformedURLException {
        // Setup context :
        Context.set(new EmptySampleContextObject(
                "http://ssrf-redirects.testssandbox.com/ssrf", // argument
                "http://ssrf-redirects.testssandbox.com/examplesite")); // url


        URLCollector.report(new URL("http://ssrf-redirects.testssandbox.com/ssrf-test"));
        RedirectCollector.report(new URL("http://ssrf-redirects.testssandbox.com/ssrf-test"), new URL("http://localhost"));
        Attack attackData = new SSRFDetector().run(
                "localhost", 80,
                List.of("127.0.0.1"),
                "test2nd_op"
        );

        assertNull(attackData);
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token")
    public void testSsrfDetectorWithServiceHostnameInRedirect() throws MalformedURLException {
        // Setup context :
        setContextAndLifecycle("http://mysql-database/ssrf-test");

        URLCollector.report(new URL("http://mysql-database/ssrf-test"));
        RedirectCollector.report(new URL("http://mysql-database/ssrf-test"), new URL("http://127.0.0.1:8080"));
        Attack attackData = new SSRFDetector().run(
            "127.0.0.1", 8080,
            List.of("127.0.0.1"),
            "testop"
        );

        assertNull(attackData);
    }
}
