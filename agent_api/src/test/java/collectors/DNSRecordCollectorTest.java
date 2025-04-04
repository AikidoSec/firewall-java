package collectors;

import dev.aikido.agent_api.collectors.DNSRecordCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.storage.Hostnames;
import dev.aikido.agent_api.storage.HostnamesStore;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import dev.aikido.agent_api.vulnerabilities.ssrf.SSRFException;
import org.junit.jupiter.api.*;
import utils.EmptySampleContextObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class DNSRecordCollectorTest {
    InetAddress inetAddress1;
    InetAddress inetAddress2;

    @BeforeEach
    void setup() throws UnknownHostException {
        // We want to define InetAddresses here so it does not interfere with counts of getHostname()
        inetAddress1 = InetAddress.getByName("1.1.1.1");
        inetAddress2 = InetAddress.getByName("127.0.0.1");
    }

    @AfterEach
    public void cleanup() {
        HostnamesStore.clear();
        Context.set(null);
    }

    @Test
    public void testContextNull() {
        // Early return because of Context being null :
        DNSRecordCollector.report("dev.aikido", new InetAddress[]{
                inetAddress1, inetAddress2
        });
    }

    @Test
    public void testThreadCacheHostnames() {
        ContextObject myContextObject = mock(ContextObject.class);
        Context.set(myContextObject);
        DNSRecordCollector.report("dev.aikido", new InetAddress[]{
                inetAddress1, inetAddress2
        });
        verify(myContextObject).getHostnames();

        myContextObject = mock(ContextObject.class);
        Hostnames hostnames = new Hostnames(20);
        when(myContextObject.getHostnames()).thenReturn(hostnames);

        Context.set(myContextObject);

        DNSRecordCollector.report("dev.aikido", new InetAddress[]{
                inetAddress1, inetAddress2
        });
        verify(myContextObject, times(2)).getHostnames();
    }

    @Test
    public void testHostnameSame() {
        ContextObject myContextObject = mock(ContextObject.class);
        Hostnames hostnames = new Hostnames(20);
        hostnames.add("dev.aikido.not", 80);
        hostnames.add("dev.aikido", 80);
        when(myContextObject.getHostnames()).thenReturn(hostnames);

        Context.set(myContextObject);
        DNSRecordCollector.report("dev.aikido", new InetAddress[]{
                inetAddress1, inetAddress2
        });
        verify(myContextObject, atLeastOnce()).getHostnames();
    }

    public static class SampleContextObject extends EmptySampleContextObject {
        public SampleContextObject() {
            super();
            this.query.put("search", List.of("example", "dev.aikido:80"));
            this.cookies.put("sessionId", List.of("dev.aikido"));
        }
    }

    @Test
    public void testHostnameSameWithContextAsAttack() {
        ServiceConfigStore.updateBlocking(true);

        ContextObject myContextObject = new SampleContextObject();
        myContextObject.getHostnames().add("dev.aikido.not", 80);
        myContextObject.getHostnames().add("dev.aikido", 80);
        Context.set(myContextObject);

        Exception exception = assertThrows(SSRFException.class, () -> {
            DNSRecordCollector.report("dev.aikido", new InetAddress[]{
                    inetAddress1, inetAddress2
            });
        });
        assertEquals("Aikido Zen has blocked a server-side request forgery", exception.getMessage());
    }
}
