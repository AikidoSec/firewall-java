package wrappers;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.storage.ConfigStore;
import dev.aikido.agent_api.storage.Hostnames;
import dev.aikido.agent_api.storage.HostnamesStore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import utils.EmptySampleContextObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class URLConnectionTest {
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
        ConfigStore.updateBlocking(true);
    }

    private void setContextAndLifecycle(String url) {
        Context.set(new EmptySampleContextObject(url));
    }

    @Test
    public void testNewUrlConnectionWithPort() throws IOException {
        setContextAndLifecycle("");

        URLConnection urlConnection = new URL("http://localhost:8080").openConnection();
        Hostnames.HostnameEntry[] hostnameArray = HostnamesStore.getHostnamesAsList();
        assertEquals(1, hostnameArray.length);
        assertEquals(8080, hostnameArray[0].getPort());
        assertEquals("localhost", hostnameArray[0].getHostname());
    }

    @Test
    public void testNewUrlConnectionWithHttp() throws IOException {
        setContextAndLifecycle("");

        URLConnection urlConnection = new URL("http://app.local.aikido.io").openConnection();
        Hostnames.HostnameEntry[] hostnameArray = HostnamesStore.getHostnamesAsList();
        assertEquals(1, hostnameArray.length);
        assertEquals(80, hostnameArray[0].getPort());
        assertEquals("app.local.aikido.io", hostnameArray[0].getHostname());
    }

    @Test
    public void testNewUrlConnectionWithHttpAsHttp() throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://app.local.aikido.io").openConnection();
        Hostnames.HostnameEntry[] hostnameArray = HostnamesStore.getHostnamesAsList();
        assertEquals(1, hostnameArray.length);
        assertEquals(80, hostnameArray[0].getPort());
        assertEquals("app.local.aikido.io", hostnameArray[0].getHostname());
    }

    @Test
    public void testNewUrlConnectionHttps() throws IOException {
        setContextAndLifecycle("");

        URLConnection urlConnection = new URL("https://aikido.dev").openConnection();
        Hostnames.HostnameEntry[] hostnameArray = HostnamesStore.getHostnamesAsList();
        assertEquals(1, hostnameArray.length);
        assertEquals(443, hostnameArray[0].getPort());
        assertEquals("aikido.dev", hostnameArray[0].getHostname());
    }

    @Test
    public void testNewUrlConnectionFaultyProtocol() throws IOException {
        setContextAndLifecycle("");

        URLConnection urlConnection = new URL("ftp://localhost:8080").openConnection();
        Hostnames.HostnameEntry[] hostnameArray = HostnamesStore.getHostnamesAsList();
        assertEquals(0, hostnameArray.length);
    }
}