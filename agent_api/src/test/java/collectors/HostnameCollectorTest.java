package collectors;

import static dev.aikido.agent_api.helpers.UnixTimeMS.getUnixTimeMS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import dev.aikido.agent_api.collectors.HostnameCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.storage.Hostnames;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;
import dev.aikido.agent_api.vulnerabilities.ssrf.SSRFException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import utils.EmptySampleContextObject;

public class HostnameCollectorTest {
    InetAddress inetAddress1;
    InetAddress inetAddress2;

    @BeforeEach
    void setup() throws UnknownHostException {
        // We want to define InetAddresses here so it does not interfere with counts of getHostname()
        inetAddress1 = InetAddress.getByName("1.1.1.1");
        inetAddress2 = InetAddress.getByName("127.0.0.1");
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "token")
    @Test
    public void testThreadCacheNull() {
        // Early return because of Thread Cache being null :
        HostnameCollector.report("dev.aikido", new InetAddress[] {inetAddress1, inetAddress2});
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "token")
    @Test
    public void testThreadCacheHostnames() {
        ThreadCacheObject myThreadCache = mock(ThreadCacheObject.class);
        when(myThreadCache.getLastRenewedAtMS()).thenReturn(getUnixTimeMS());
        ThreadCache.set(myThreadCache);
        HostnameCollector.report("dev.aikido", new InetAddress[] {inetAddress1, inetAddress2});
        verify(myThreadCache).getHostnames();

        myThreadCache = mock(ThreadCacheObject.class);
        when(myThreadCache.getLastRenewedAtMS()).thenReturn(getUnixTimeMS());

        Hostnames hostnames = new Hostnames(20);
        when(myThreadCache.getHostnames()).thenReturn(hostnames);

        ThreadCache.set(myThreadCache);
        HostnameCollector.report("dev.aikido", new InetAddress[] {inetAddress1, inetAddress2});
        verify(myThreadCache, times(2)).getHostnames();
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "token")
    @Test
    public void testHostnameSame() {
        ThreadCacheObject myThreadCache = mock(ThreadCacheObject.class);
        when(myThreadCache.getLastRenewedAtMS()).thenReturn(getUnixTimeMS());

        Hostnames hostnames = new Hostnames(20);
        hostnames.add("dev.aikido.not", 80);
        hostnames.add("dev.aikido", 80);
        when(myThreadCache.getHostnames()).thenReturn(hostnames);

        ThreadCache.set(myThreadCache);
        HostnameCollector.report("dev.aikido", new InetAddress[] {inetAddress1, inetAddress2});
        verify(myThreadCache, times(2)).getHostnames();
    }

    public static class SampleContextObject extends EmptySampleContextObject {
        public SampleContextObject() {
            super();
            this.query.put("search", List.of("example", "dev.aikido:80"));
            this.cookies.put("sessionId", List.of("dev.aikido"));
        }
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCK", value = "1")
    @Test
    public void testHostnameSameWithContextAsAttack() {
        ThreadCacheObject myThreadCache = mock(ThreadCacheObject.class);
        when(myThreadCache.getLastRenewedAtMS()).thenReturn(getUnixTimeMS());

        Hostnames hostnames = new Hostnames(20);
        hostnames.add("dev.aikido.not", 80);
        hostnames.add("dev.aikido", 80);
        when(myThreadCache.getHostnames()).thenReturn(hostnames);

        ThreadCache.set(myThreadCache);
        Context.set(new SampleContextObject());
        Exception exception = assertThrows(SSRFException.class, () -> {
            HostnameCollector.report("dev.aikido", new InetAddress[] {inetAddress1, inetAddress2});
        });
        verify(myThreadCache, times(2)).getHostnames();
        assertEquals("Aikido Zen has blocked a server-side request forgery", exception.getMessage());
    }

    @AfterEach
    public void cleanup() {
        ThreadCache.set(null);
        Context.set(null);
    }
}
