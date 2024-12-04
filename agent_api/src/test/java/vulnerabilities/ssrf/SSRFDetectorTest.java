package vulnerabilities.ssrf;

import dev.aikido.agent_api.collectors.RedirectCollector;
import dev.aikido.agent_api.collectors.URLCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.storage.routes.Routes;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;
import dev.aikido.agent_api.vulnerabilities.Attack;
import dev.aikido.agent_api.vulnerabilities.ssrf.SSRFDetector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import wrappers.HttpURLConnectionRedirectTest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SSRFDetectorTest {
    public static class SampleContextObject extends ContextObject {
        public SampleContextObject(String argument) {
            this.redirectStartNodes = new ArrayList<>();
            this.method = "GET";
            this.source = "web";
            this.url = "https://example.com/api/resource";
            this.route = "/api/resource";
            this.remoteAddress = "192.168.1.1";
            this.headers = new HashMap<>();

            this.query = new HashMap<>();
            this.query.put("search", new String[]{"example", "dev.aikido:80"});
            this.query.put("sql1", new String[]{"SELECT * FRO"});
            this.query.put("arg", new String[]{argument});

            this.cookies = new HashMap<>();
            this.body = "{\"key\":\"value\"}"; // Body as a JSON string
        }
    }
    @BeforeAll
    static void cleanup() {
        Context.set(null);
        ThreadCache.set(null);
    }
    @AfterAll
    static void afterAll() {
        cleanup();
    }

    private void setContextAndLifecycle(String url) {
        Context.set(new SampleContextObject(url));
        ThreadCache.set(new ThreadCacheObject(List.of(), Set.of(), Set.of(), new Routes()));
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
}
