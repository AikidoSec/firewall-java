package wrappers;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.storage.routes.Routes;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HttpURLConnectionRedirectTest {

    private static final String SSRF_TEST = "http://ssrf-redirects.testssandbox.com/ssrf-test";
    private static final String SSRF_TEST_DOMAIN = "http://ssrf-redirects.testssandbox.com/ssrf-test-domain";
    private static final String SSRF_TEST_TWICE = "http://ssrf-redirects.testssandbox.com/ssrf-test-twice";
    private static final String SSRF_TEST_DOMAIN_TWICE = "http://ssrf-redirects.testssandbox.com/ssrf-test-domain-twice";
    private static final String CROSS_DOMAIN_TEST = "http://firewallssrfredirects-env-2.eba-7ifve22q.eu-north-1.elasticbeanstalk.com/ssrf-test";
    private static final String CROSS_DOMAIN_TEST_DOMAIN_TWICE = "http://firewallssrfredirects-env-2.eba-7ifve22q.eu-north-1.elasticbeanstalk.com/ssrf-test-domain-twice";

    public static class SampleContextObject extends ContextObject {
        public SampleContextObject(String argument) {
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
            this.body = new HashMap<>();
            this.body.put("test", "{\"key\":\"value\"}"); // Body as a JSON string
            this.redirectStartNodes = new ArrayList<>();
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

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    public void testSrrfTest() {
        setContextAndLifecycle(SSRF_TEST);

        Exception exception1 = assertThrows(RuntimeException.class, () -> {
            URL url = new URL(SSRF_TEST);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);

            connection.getResponseCode();
        });
        assertEquals(
                "dev.aikido.agent_api.vulnerabilities.ssrf.SSRFException: Aikido Zen has blocked a server-side request forgery",
                exception1.getMessage());
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    public void testSrrfTestTwice() {
        setContextAndLifecycle(SSRF_TEST_TWICE);

        Exception exception1 = assertThrows(RuntimeException.class, () -> {
            URL url = new URL(SSRF_TEST_TWICE);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);

            connection.getResponseCode();
        });
        assertEquals(
                "dev.aikido.agent_api.vulnerabilities.ssrf.SSRFException: Aikido Zen has blocked a server-side request forgery",
                exception1.getMessage());
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    public void testSrrfTestDomain() {
        setContextAndLifecycle(SSRF_TEST_DOMAIN);

        Exception exception1 = assertThrows(RuntimeException.class, () -> {
            URL url = new URL(SSRF_TEST_DOMAIN);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);

            connection.getResponseCode();
        });
        assertEquals(
                "dev.aikido.agent_api.vulnerabilities.ssrf.SSRFException: Aikido Zen has blocked a server-side request forgery",
                exception1.getMessage());
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    public void testSrrfTestDomainTwice() {
        setContextAndLifecycle(SSRF_TEST_DOMAIN_TWICE);

        Exception exception1 = assertThrows(RuntimeException.class, () -> {
            URL url = new URL(SSRF_TEST_DOMAIN_TWICE);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);

            connection.getResponseCode();
        });
        assertEquals(
                "dev.aikido.agent_api.vulnerabilities.ssrf.SSRFException: Aikido Zen has blocked a server-side request forgery",
                exception1.getMessage());
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    public void testSsrfCrossDomain() {
        setContextAndLifecycle(CROSS_DOMAIN_TEST);

        Exception exception1 = assertThrows(RuntimeException.class, () -> {
            URL url = new URL(CROSS_DOMAIN_TEST);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);

            connection.getResponseCode();
        });
        assertEquals(
                "dev.aikido.agent_api.vulnerabilities.ssrf.SSRFException: Aikido Zen has blocked a server-side request forgery",
                exception1.getMessage());
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    public void testSsrfCrossDomainTwice() {
        setContextAndLifecycle(CROSS_DOMAIN_TEST_DOMAIN_TWICE);

        Exception exception1 = assertThrows(RuntimeException.class, () -> {
            URL url = new URL(CROSS_DOMAIN_TEST_DOMAIN_TWICE);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);

            connection.getResponseCode();
        });
        assertEquals(
                "dev.aikido.agent_api.vulnerabilities.ssrf.SSRFException: Aikido Zen has blocked a server-side request forgery",
                exception1.getMessage());
    }
}