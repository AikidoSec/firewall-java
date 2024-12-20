package collectors;

import dev.aikido.agent_api.collectors.URLCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.storage.Hostnames;
import dev.aikido.agent_api.storage.routes.Routes;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class URLCollectorTest {

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
            this.body = "{\"key\":\"value\"}"; // Body as a JSON string
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
    public void testNewUrlConnectionWithPort() throws IOException {
        setContextAndLifecycle("");
        
        URLCollector.report(new URL("http://localhost:8080"));
        Hostnames.HostnameEntry[] hostnameArray = ThreadCache.get().getHostnames().asArray();
        assertEquals(1, hostnameArray.length);
        assertEquals(8080, hostnameArray[0].getPort());
        assertEquals("localhost", hostnameArray[0].getHostname());
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    public void testNewUrlConnectionWithHttp() throws IOException {
        setContextAndLifecycle("");
        URLCollector.report(new URL("http://app.local.aikido.io"));
        Hostnames.HostnameEntry[] hostnameArray = ThreadCache.get().getHostnames().asArray();
        assertEquals(1, hostnameArray.length);
        assertEquals(80, hostnameArray[0].getPort());
        assertEquals("app.local.aikido.io", hostnameArray[0].getHostname());
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    public void testNewUrlConnectionHttps() throws IOException {
        setContextAndLifecycle("");
        URLCollector.report(new URL("https://aikido.dev"));
        Hostnames.HostnameEntry[] hostnameArray = ThreadCache.get().getHostnames().asArray();
        assertEquals(1, hostnameArray.length);
        assertEquals(443, hostnameArray[0].getPort());
        assertEquals("aikido.dev", hostnameArray[0].getHostname());
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCKING", value = "true")
    @Test
    public void testNewUrlConnectionFaultyProtocol() throws IOException {
        setContextAndLifecycle("");
        URLCollector.report(new URL("ftp://localhost:8080"));
        Hostnames.HostnameEntry[] hostnameArray = ThreadCache.get().getHostnames().asArray();
        assertEquals(0, hostnameArray.length);
    }
}