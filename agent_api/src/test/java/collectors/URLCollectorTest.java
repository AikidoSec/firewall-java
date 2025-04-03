package collectors;

import dev.aikido.agent_api.collectors.URLCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.storage.Hostnames;
import dev.aikido.agent_api.storage.HostnamesStore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.EmptySampleContextObject;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class URLCollectorTest {
    @BeforeAll
    static void cleanup() {
        Context.set(null);
        HostnamesStore.clear();
    }
    @AfterAll
    static void afterAll() {
        cleanup();
    }
    @BeforeEach
    void beforeEach() {
        cleanup();
    }

    private void setContextAndLifecycle(String url) {
        Context.set(new EmptySampleContextObject(url));
    }

    @Test
    public void testNewUrlConnectionWithPort() throws IOException {
        setContextAndLifecycle("");
        
        URLCollector.report(new URL("http://localhost:8080"));
        Hostnames.HostnameEntry[] hostnameArray = HostnamesStore.getHostnamesAsList();
        assertEquals(1, hostnameArray.length);
        assertEquals(8080, hostnameArray[0].getPort());
        assertEquals("localhost", hostnameArray[0].getHostname());
    }

    @Test
    public void testNewUrlConnectionWithHttp() throws IOException {
        setContextAndLifecycle("");
        URLCollector.report(new URL("http://app.local.aikido.io"));
        Hostnames.HostnameEntry[] hostnameArray = HostnamesStore.getHostnamesAsList();
        assertEquals(1, hostnameArray.length);
        assertEquals(80, hostnameArray[0].getPort());
        assertEquals("app.local.aikido.io", hostnameArray[0].getHostname());

        Hostnames.HostnameEntry[] hostnameArray2 = Context.get().getHostnames().asArray();
        assertEquals(1, hostnameArray2.length);
        assertEquals(80, hostnameArray2[0].getPort());
        assertEquals("app.local.aikido.io", hostnameArray2[0].getHostname());
    }

    @Test
    public void testNewUrlConnectionHttps() throws IOException {
        setContextAndLifecycle("");
        URLCollector.report(new URL("https://aikido.dev"));
        Hostnames.HostnameEntry[] hostnameArray = HostnamesStore.getHostnamesAsList();
        assertEquals(1, hostnameArray.length);
        assertEquals(443, hostnameArray[0].getPort());
        assertEquals("aikido.dev", hostnameArray[0].getHostname());

        Hostnames.HostnameEntry[] hostnameArray2 = Context.get().getHostnames().asArray();
        assertEquals(1, hostnameArray2.length);
        assertEquals(443, hostnameArray2[0].getPort());
        assertEquals("aikido.dev", hostnameArray2[0].getHostname());
    }

    @Test
    public void testNewUrlConnectionFaultyProtocol() throws IOException {
        setContextAndLifecycle("");
        URLCollector.report(new URL("ftp://localhost:8080"));
        Hostnames.HostnameEntry[] hostnameArray = HostnamesStore.getHostnamesAsList();
        assertEquals(0, hostnameArray.length);
        Hostnames.HostnameEntry[] hostnameArray2 = Context.get().getHostnames().asArray();
        assertEquals(0, hostnameArray2.length);
    }

    @Test
    public void testWithNullURL() throws IOException {
        setContextAndLifecycle("");
        URLCollector.report(null);
        Hostnames.HostnameEntry[] hostnameArray = HostnamesStore.getHostnamesAsList();
        assertEquals(0, hostnameArray.length);
        Hostnames.HostnameEntry[] hostnameArray2 = Context.get().getHostnames().asArray();
        assertEquals(0, hostnameArray2.length);
    }

    @Test
    public void testWithNullContext() throws IOException {
        setContextAndLifecycle("");
        Context.reset();
        URLCollector.report(new URL("https://aikido.dev"));
        Hostnames.HostnameEntry[] hostnameArray = HostnamesStore.getHostnamesAsList();
        assertEquals(1, hostnameArray.length);
        assertEquals(443, hostnameArray[0].getPort());
        assertEquals("aikido.dev", hostnameArray[0].getHostname());
        assertNull(Context.get());
    }

    @Test
    public void testOnlyContext() throws IOException {
        setContextAndLifecycle("");
        HostnamesStore.clear();
        URLCollector.report(new URL("https://aikido.dev"));
        Hostnames.HostnameEntry[] hostnameArray = Context.get().getHostnames().asArray();
        assertEquals(1, hostnameArray.length);
        assertEquals(443, hostnameArray[0].getPort());
        assertEquals("aikido.dev", hostnameArray[0].getHostname());
    }
}