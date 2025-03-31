package wrappers;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.storage.HostnamesStore;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import utils.EmptySampleContextObject;

import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static utils.EmtpyThreadCacheObject.getEmptyThreadCacheObject;

public class HttpURLConnectionRedirectTest {

    private static final String SSRF_TEST = "http://ssrf-redirects.testssandbox.com/ssrf-test";
    private static final String SSRF_TEST_DOMAIN = "http://ssrf-redirects.testssandbox.com/ssrf-test-domain";
    private static final String SSRF_TEST_TWICE = "http://ssrf-redirects.testssandbox.com/ssrf-test-twice";
    private static final String SSRF_TEST_DOMAIN_TWICE = "http://ssrf-redirects.testssandbox.com/ssrf-test-domain-twice";
    private static final String CROSS_DOMAIN_TEST = "http://firewallssrfredirects-env-2.eba-7ifve22q.eu-north-1.elasticbeanstalk.com/ssrf-test";
    private static final String CROSS_DOMAIN_TEST_DOMAIN_TWICE = "http://firewallssrfredirects-env-2.eba-7ifve22q.eu-north-1.elasticbeanstalk.com/ssrf-test-domain-twice";

    @BeforeAll
    static void cleanup() {
        Context.set(null);
        HostnamesStore.clear();
    }
    @AfterAll
    static void afterAll() {
        cleanup();
    }

    private void setContextAndLifecycle(String url) {
        Context.set(new EmptySampleContextObject(url));
        HostnamesStore.clear();
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCK", value = "true")
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
    @SetEnvironmentVariable(key = "AIKIDO_BLOCK", value = "true")
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
    @SetEnvironmentVariable(key = "AIKIDO_BLOCK", value = "true")
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
    @SetEnvironmentVariable(key = "AIKIDO_BLOCK", value = "true")
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
    @SetEnvironmentVariable(key = "AIKIDO_BLOCK", value = "true")
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
    @SetEnvironmentVariable(key = "AIKIDO_BLOCK", value = "true")
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