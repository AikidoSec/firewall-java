package wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static utils.EmtpyThreadCacheObject.getEmptyThreadCacheObject;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.storage.Hostnames;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import utils.EmptySampleContextObject;

public class URLConnectionTest {
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
        Context.set(new EmptySampleContextObject(url));
        ThreadCache.set(getEmptyThreadCacheObject());
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCK", value = "true")
    @Test
    public void testNewUrlConnectionWithPort() throws IOException {
        setContextAndLifecycle("");

        URLConnection urlConnection = new URL("http://localhost:8080").openConnection();
        Hostnames.HostnameEntry[] hostnameArray =
                ThreadCache.get().getHostnames().asArray();
        assertEquals(1, hostnameArray.length);
        assertEquals(8080, hostnameArray[0].getPort());
        assertEquals("localhost", hostnameArray[0].getHostname());
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCK", value = "true")
    @Test
    public void testNewUrlConnectionWithHttp() throws IOException {
        setContextAndLifecycle("");

        URLConnection urlConnection = new URL("http://app.local.aikido.io").openConnection();
        Hostnames.HostnameEntry[] hostnameArray =
                ThreadCache.get().getHostnames().asArray();
        assertEquals(1, hostnameArray.length);
        assertEquals(80, hostnameArray[0].getPort());
        assertEquals("app.local.aikido.io", hostnameArray[0].getHostname());
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCK", value = "true")
    @Test
    public void testNewUrlConnectionWithHttpAsHttp() throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://app.local.aikido.io").openConnection();
        Hostnames.HostnameEntry[] hostnameArray =
                ThreadCache.get().getHostnames().asArray();
        assertEquals(1, hostnameArray.length);
        assertEquals(80, hostnameArray[0].getPort());
        assertEquals("app.local.aikido.io", hostnameArray[0].getHostname());
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCK", value = "true")
    @Test
    public void testNewUrlConnectionHttps() throws IOException {
        setContextAndLifecycle("");

        URLConnection urlConnection = new URL("https://aikido.dev").openConnection();
        Hostnames.HostnameEntry[] hostnameArray =
                ThreadCache.get().getHostnames().asArray();
        assertEquals(1, hostnameArray.length);
        assertEquals(443, hostnameArray[0].getPort());
        assertEquals("aikido.dev", hostnameArray[0].getHostname());
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCK", value = "true")
    @Test
    public void testNewUrlConnectionFaultyProtocol() throws IOException {
        setContextAndLifecycle("");

        URLConnection urlConnection = new URL("ftp://localhost:8080").openConnection();
        Hostnames.HostnameEntry[] hostnameArray =
                ThreadCache.get().getHostnames().asArray();
        assertEquals(0, hostnameArray.length);
    }
}
