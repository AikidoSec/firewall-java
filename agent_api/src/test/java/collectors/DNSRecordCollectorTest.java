package collectors;

import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.events.DetectedAttack;
import dev.aikido.agent_api.collectors.DNSRecordCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.storage.AttackQueue;
import dev.aikido.agent_api.storage.BypassedContextStore;
import dev.aikido.agent_api.storage.Hostnames;
import dev.aikido.agent_api.storage.HostnamesStore;
import dev.aikido.agent_api.storage.PendingHostnamesStore;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import dev.aikido.agent_api.storage.service_configuration.Domain;
import dev.aikido.agent_api.vulnerabilities.outbound_blocking.BlockedOutboundException;
import dev.aikido.agent_api.vulnerabilities.ssrf.SSRFException;
import dev.aikido.agent_api.vulnerabilities.ssrf.StoredSSRFException;
import org.junit.jupiter.api.*;
import utils.EmptySampleContextObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DNSRecordCollectorTest {
    InetAddress inetAddress1;
    InetAddress inetAddress2;
    InetAddress imdsAddress1;

    @BeforeEach
    void setup() throws UnknownHostException {
        inetAddress1 = InetAddress.getByName("1.1.1.1");
        inetAddress2 = InetAddress.getByName("127.0.0.1");
        imdsAddress1 = InetAddress.getByName("169.254.169.254");
        AttackQueue.clear();
        HostnamesStore.clear();
        PendingHostnamesStore.clear();
        BypassedContextStore.clear();
    }

    @AfterEach
    public void cleanup() {
        HostnamesStore.clear();
        PendingHostnamesStore.clear();
        Context.set(null);
        AttackQueue.clear();
        BypassedContextStore.clear();
        // Reset domain config
        ServiceConfigStore.updateFromAPIResponse(new APIResponse(
            true, null, 0L, null, null, null, false, List.of(), true, false, List.of()
        ));
    }

    public static class SampleContextObject extends EmptySampleContextObject {
        public SampleContextObject() {
            super();
            this.query.put("search", List.of("example", "dev.aikido:80"));
            this.cookies.put("sessionId", List.of("dev.aikido"));
        }
    }

    @Test
    public void testNoPendingHostnames() {
        // No pending hostnames → port 0 recorded, no SSRF check
        Context.set(new EmptySampleContextObject());
        DNSRecordCollector.report("dev.aikido", new InetAddress[]{inetAddress1, inetAddress2});
        Hostnames.HostnameEntry[] entries = HostnamesStore.getHostnamesAsList();
        assertEquals(1, entries.length);
        assertEquals("dev.aikido", entries[0].getHostname());
        assertEquals(0, entries[0].getPort());
    }

    @Test
    public void testPendingHostnameOtherThanLookedUp() {
        // A pending entry for a different hostname should not affect the looked-up hostname
        PendingHostnamesStore.add("dev.aikido.not", 80);
        Context.set(new EmptySampleContextObject());
        DNSRecordCollector.report("dev.aikido", new InetAddress[]{inetAddress1, inetAddress2});
        Hostnames.HostnameEntry[] entries = HostnamesStore.getHostnamesAsList();
        assertEquals(1, entries.length);
        assertEquals("dev.aikido", entries[0].getHostname());
        assertEquals(0, entries[0].getPort());
    }

    @Test
    public void testSSRFWithPendingHostname() {
        ServiceConfigStore.updateBlocking(true);

        PendingHostnamesStore.add("dev.aikido", 80);
        Context.set(new SampleContextObject());

        Exception exception = assertThrows(SSRFException.class, () -> {
            DNSRecordCollector.report("dev.aikido", new InetAddress[]{inetAddress1, inetAddress2});
        });
        assertEquals("Aikido Zen has blocked a server-side request forgery", exception.getMessage());
    }

    @Test
    public void testHostnameSameWithContextAsAStoredSSRFAttack() {
        ServiceConfigStore.updateBlocking(true);

        Context.set(new SampleContextObject());

        Exception exception = assertThrows(StoredSSRFException.class, () -> {
            DNSRecordCollector.report("dev.aikido", new InetAddress[]{imdsAddress1, inetAddress2});
        });
        assertEquals("Aikido Zen has blocked a stored server-side request forgery", exception.getMessage());

        assertDoesNotThrow(() -> {
            DNSRecordCollector.report("metadata.goog", new InetAddress[]{imdsAddress1, inetAddress2});
            DNSRecordCollector.report("metadata.google.internal", new InetAddress[]{imdsAddress1, inetAddress2});
        });
    }

    @Test
    public void testBlockedDomain() {
        ServiceConfigStore.updateFromAPIResponse(new APIResponse(
            true, null, 0L, null, null, null,
            false, List.of(new Domain("blocked.example.com", "block")), true, true, List.of()
        ));
        assertThrows(BlockedOutboundException.class, () ->
            DNSRecordCollector.report("blocked.example.com", new InetAddress[]{inetAddress1})
        );
    }

    @Test
    public void testAllowedDomainNotBlocked() {
        ServiceConfigStore.updateFromAPIResponse(new APIResponse(
            true, null, 0L, null, null, null,
            false, List.of(new Domain("allowed.example.com", "allow")), true, true, List.of()
        ));
        assertDoesNotThrow(() ->
            DNSRecordCollector.report("allowed.example.com", new InetAddress[]{inetAddress1})
        );
    }

    @Test
    public void testBlockedDomainNotBlockedWhenIpBypassed() {
        ServiceConfigStore.updateFromAPIResponse(new APIResponse(
            true, null, 0L, null, null, null,
            false, List.of(new Domain("blocked.example.com", "block")), true, true, List.of()
        ));
        BypassedContextStore.setBypassed(true);
        assertDoesNotThrow(() ->
            DNSRecordCollector.report("blocked.example.com", new InetAddress[]{inetAddress1})
        );
    }

    @Test
    public void testHostnamesStoreNotUpdatedWhenBypassed() {
        BypassedContextStore.setBypassed(true);
        Context.set(new EmptySampleContextObject());

        DNSRecordCollector.report("dev.aikido", new InetAddress[]{inetAddress1});

        assertEquals(0, HostnamesStore.getHostnamesAsList().length);
    }

    @Test
    public void testHostnamesStoreNotUpdatedWhenBypassedWithPendingPorts() {
        PendingHostnamesStore.add("dev.aikido", 80);
        PendingHostnamesStore.add("dev.aikido", 443);
        BypassedContextStore.setBypassed(true);
        Context.set(mock(ContextObject.class));

        DNSRecordCollector.report("dev.aikido", new InetAddress[]{inetAddress1});

        assertEquals(0, HostnamesStore.getHostnamesAsList().length);
        // Pending entries are still consumed even when bypassed so the store doesn't grow unboundedly
        assertTrue(PendingHostnamesStore.getPorts("dev.aikido").isEmpty());
    }

    @Test
    public void testUnknownDomainBlockedWhenBlockNewOutgoingRequests() {
        ServiceConfigStore.updateFromAPIResponse(new APIResponse(
            true, null, 0L, null, null, null,
            true, List.of(), true, true, List.of()
        ));
        assertThrows(BlockedOutboundException.class, () ->
            DNSRecordCollector.report("unknown.example.com", new InetAddress[]{inetAddress1})
        );
    }

    @Test
    public void testHostnamesStorePort0WhenNoPendingEntry() {
        Context.set(null);
        DNSRecordCollector.report("dev.aikido", new InetAddress[]{inetAddress1});
        Hostnames.HostnameEntry[] entries = HostnamesStore.getHostnamesAsList();
        assertEquals(1, entries.length);
        assertEquals("dev.aikido", entries[0].getHostname());
        assertEquals(0, entries[0].getPort());
    }

    @Test
    public void testHostnamesStoreUsesPortFromPendingStore() {
        PendingHostnamesStore.add("dev.aikido", 8080);
        Context.set(mock(ContextObject.class));

        DNSRecordCollector.report("dev.aikido", new InetAddress[]{inetAddress1});
        Hostnames.HostnameEntry[] entries = HostnamesStore.getHostnamesAsList();
        assertEquals(1, entries.length);
        assertEquals("dev.aikido", entries[0].getHostname());
        assertEquals(8080, entries[0].getPort());
    }

    @Test
    public void testHostnamesStoreIncrementedForAllPendingPorts() {
        PendingHostnamesStore.add("dev.aikido", 80);
        PendingHostnamesStore.add("dev.aikido", 443);
        Context.set(mock(ContextObject.class));

        DNSRecordCollector.report("dev.aikido", new InetAddress[]{inetAddress1});
        Hostnames.HostnameEntry[] entries = HostnamesStore.getHostnamesAsList();
        assertEquals(2, entries.length);
        assertEquals(80, entries[0].getPort());
        assertEquals(443, entries[1].getPort());
    }

    @Test
    public void testPendingEntryRemovedAfterDNSLookup() {
        PendingHostnamesStore.add("dev.aikido", 8080);
        Context.set(mock(ContextObject.class));

        DNSRecordCollector.report("dev.aikido", new InetAddress[]{inetAddress1});
        // Entry should have been consumed
        assertTrue(PendingHostnamesStore.getPorts("dev.aikido").isEmpty());
    }

    @Test
    public void testSSRFStillRunsWhenPendingPortIsZero() {
        ServiceConfigStore.updateBlocking(true);

        PendingHostnamesStore.add("dev.aikido", 0);
        Context.set(new SampleContextObject());

        Exception exception = assertThrows(SSRFException.class, () -> {
            DNSRecordCollector.report("dev.aikido", new InetAddress[]{inetAddress1, inetAddress2});
        });
        assertEquals("Aikido Zen has blocked a server-side request forgery", exception.getMessage());
    }

    @Test
    public void testStoredSSRFWithNoContext() throws InterruptedException {
        ServiceConfigStore.updateBlocking(true);

        Context.set(null);

        Exception exception = assertThrows(StoredSSRFException.class, () -> {
            DNSRecordCollector.report("dev.aikido", new InetAddress[]{imdsAddress1, inetAddress2});
        });
        DetectedAttack.DetectedAttackEvent event = (DetectedAttack.DetectedAttackEvent) AttackQueue.get();
        assertEquals("stored_ssrf", event.attack().kind());
        assertNull(event.request());

        assertEquals("Aikido Zen has blocked a stored server-side request forgery", exception.getMessage());

        assertDoesNotThrow(() -> {
            DNSRecordCollector.report("metadata.goog", new InetAddress[]{imdsAddress1, inetAddress2});
            DNSRecordCollector.report("metadata.google.internal", new InetAddress[]{imdsAddress1, inetAddress2});
        });
    }
}
