package collectors;

import dev.aikido.agent_api.collectors.URLCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.storage.HostnamesStore;
import dev.aikido.agent_api.storage.PendingHostnamesStore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.EmptySampleContextObject;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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
        PendingHostnamesStore.clear();
    }

    private void setContextAndLifecycle(String url) {
        Context.set(new EmptySampleContextObject(url));
    }

    @Test
    public void testNewUrlConnectionWithPort() throws IOException {
        setContextAndLifecycle("");

        URLCollector.report(new URL("http://localhost:8080"));
        Set<Integer> ports = PendingHostnamesStore.getPorts("localhost");
        assertEquals(1, ports.size());
        assertTrue(ports.contains(8080));
    }

    @Test
    public void testNewUrlConnectionWithHttp() throws IOException {
        setContextAndLifecycle("");
        URLCollector.report(new URL("http://app.local.aikido.io"));
        Set<Integer> ports = PendingHostnamesStore.getPorts("app.local.aikido.io");
        assertEquals(1, ports.size());
        assertTrue(ports.contains(80));
    }

    @Test
    public void testNewUrlConnectionHttps() throws IOException {
        setContextAndLifecycle("");
        URLCollector.report(new URL("https://aikido.dev"));
        Set<Integer> ports = PendingHostnamesStore.getPorts("aikido.dev");
        assertEquals(1, ports.size());
        assertTrue(ports.contains(443));
    }

    @Test
    public void testNewUrlConnectionFaultyProtocol() throws IOException {
        setContextAndLifecycle("");
        URLCollector.report(new URL("ftp://localhost:8080"));
        assertEquals(0, HostnamesStore.getHostnamesAsList().length);
        assertTrue(PendingHostnamesStore.getPorts("localhost").isEmpty());
    }

    @Test
    public void testWithNullURL() throws IOException {
        setContextAndLifecycle("");
        URLCollector.report(null);
        assertEquals(0, HostnamesStore.getHostnamesAsList().length);
        assertTrue(PendingHostnamesStore.getPorts("localhost").isEmpty());
    }

    @Test
    public void testWithNullContext() throws IOException {
        setContextAndLifecycle("");
        Context.reset();
        URLCollector.report(new URL("https://aikido.dev"));
        // URLCollector writes to PendingHostnamesStore regardless of context state
        Set<Integer> ports = PendingHostnamesStore.getPorts("aikido.dev");
        assertEquals(1, ports.size());
        assertTrue(ports.contains(443));
        assertNull(Context.get());
    }

    @Test
    public void testOnlyPendingStore() throws IOException {
        setContextAndLifecycle("");
        URLCollector.report(new URL("https://aikido.dev"));
        // HostnamesStore is only written by DNSRecordCollector, not URLCollector
        assertEquals(0, HostnamesStore.getHostnamesAsList().length);
        Set<Integer> ports = PendingHostnamesStore.getPorts("aikido.dev");
        assertEquals(1, ports.size());
        assertTrue(ports.contains(443));
    }
}
