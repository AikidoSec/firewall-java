package collectors;

import dev.aikido.agent_api.background.cloud.api.APIResponse;
import dev.aikido.agent_api.background.cloud.api.events.DetectedAttack;
import dev.aikido.agent_api.collectors.DNSRecordCollector;
import dev.aikido.agent_api.collectors.RedirectCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.storage.AttackQueue;
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
import java.net.URL;
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
    }

    @AfterEach
    public void cleanup() {
        HostnamesStore.clear();
        PendingHostnamesStore.clear();
        Context.set(null);
        AttackQueue.clear();
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

    @Test
    public void testPrivateIpLiteralWithNoPendingPortNotRecorded() {
        // No pending port and the hostname is a private IP literal: infrastructure noise
        // (e.g. Reactor Netty's resolver bootstrap resolving nameserver/bind addresses).
        // Must not be recorded as an outbound hostname.
        Context.set(null);
        DNSRecordCollector.report("10.20.11.143", new InetAddress[]{inetAddress2});
        assertEquals(0, HostnamesStore.getHostnamesAsList().length);
    }

    @Test
    public void testPrivateIpLiteralWithNoPendingPortNotBlockedInLockdown() {
        // Lockdown mode (blockNewOutgoingRequests=true) must not block a private IP literal
        // that has no pending port, otherwise it would break internal/infra resolutions.
        ServiceConfigStore.updateFromAPIResponse(new APIResponse(
            true, null, 0L, null, null, null, true, List.of(), true, true, List.of()
        ));
        Context.set(null);
        assertDoesNotThrow(() ->
            DNSRecordCollector.report("10.20.11.143", new InetAddress[]{inetAddress2})
        );
        assertEquals(0, HostnamesStore.getHostnamesAsList().length);
    }

    @Test
    public void testPrivateIpLiteralWithPendingPortStillRecordedAndBlockedInLockdown() {
        // A private IP literal that DOES have a pending port came from a real outgoing
        // request made through an instrumented client, not from infrastructure noise. It
        // must still be recorded and still be subject to outbound blocking in lockdown mode.
        ServiceConfigStore.updateFromAPIResponse(new APIResponse(
            true, null, 0L, null, null, null, true, List.of(), true, true, List.of()
        ));
        PendingHostnamesStore.add("10.20.11.143", 443);
        Context.set(mock(ContextObject.class));

        assertThrows(BlockedOutboundException.class, () ->
            DNSRecordCollector.report("10.20.11.143", new InetAddress[]{inetAddress2})
        );
    }

    @Test
    public void testSsrfStillDetectedForPrivateIpLiteralWithPendingPort() {
        // Regression test: an attacker-supplied private IP literal (e.g. a webhook URL field
        // pointing straight at 169.254.169.254) reaching a real outgoing request through an
        // instrumented client must still be caught as SSRF. Earlier attempts at filtering
        // private IP literals used an early return that accidentally skipped this check.
        ServiceConfigStore.updateBlocking(true);
        PendingHostnamesStore.add("169.254.169.254", 80);
        Context.set(new EmptySampleContextObject("http://169.254.169.254:80/latest/meta-data/"));

        Exception exception = assertThrows(SSRFException.class, () ->
            DNSRecordCollector.report("169.254.169.254", new InetAddress[]{imdsAddress1})
        );
        assertEquals("Aikido Zen has blocked a server-side request forgery", exception.getMessage());
    }

    // reportConnect(): used by SocketChannelWrapper for clients that resolve their own DNS
    // (e.g. Reactor Netty, used by Spring's WebClient) instead of InetAddress.getAllByName(),
    // reporting one resolved address per connect() attempt.

    @Test
    public void testReportConnectRecordsHostnameWithPendingPort() {
        PendingHostnamesStore.add("example.com", 443);
        Context.set(mock(ContextObject.class));

        DNSRecordCollector.reportConnect("example.com", inetAddress1);
        Hostnames.HostnameEntry[] entries = HostnamesStore.getHostnamesAsList();
        assertEquals(1, entries.length);
        assertEquals("example.com", entries[0].getHostname());
        assertEquals(443, entries[0].getPort());
    }

    @Test
    public void testReportConnectDoesNotConsumePendingPort() {
        // Unlike report(), reportConnect() must peek instead of consume: a single outbound
        // request can trigger multiple connect() calls to the same hostname (e.g. trying the
        // IPv4 then the IPv6 address of a dual-stack host), and each one must still see the
        // pending port to be checked correctly.
        PendingHostnamesStore.add("example.com", 443);
        Context.set(mock(ContextObject.class));

        DNSRecordCollector.reportConnect("example.com", inetAddress1);
        assertFalse(PendingHostnamesStore.getPorts("example.com").isEmpty());

        // A second connect attempt (e.g. the IPv6 address) still sees the same pending port
        // and records another hit, instead of falling back to port 0 or being skipped.
        DNSRecordCollector.reportConnect("example.com", inetAddress2);
        Hostnames.HostnameEntry[] entries = HostnamesStore.getHostnamesAsList();
        assertEquals(1, entries.length);
        assertEquals("example.com", entries[0].getHostname());
        assertEquals(443, entries[0].getPort());
        assertEquals(2, entries[0].getHits());
    }

    @Test
    public void testSsrfDetectedOnEveryConnectAttemptForDualStackHostname() throws UnknownHostException {
        // Regression test for a real bug found via e2e testing: "localhost" resolves to both
        // 127.0.0.1 and ::1, and Reactor Netty tries both addresses via separate connect()
        // calls. With a naive getAndRemove() the first attempt would consume the pending port
        // and the second attempt would silently skip the SSRF check, letting the request
        // through despite the first attempt having been blocked.
        InetAddress loopbackIPv6 = InetAddress.getByName("::1");
        ServiceConfigStore.updateBlocking(true);
        PendingHostnamesStore.add("localhost", 5000);
        Context.set(new EmptySampleContextObject("http://localhost:5000"));

        assertThrows(SSRFException.class, () ->
            DNSRecordCollector.reportConnect("localhost", inetAddress2) // 127.0.0.1
        );
        assertThrows(SSRFException.class, () ->
            DNSRecordCollector.reportConnect("localhost", loopbackIPv6) // ::1
        );
    }

    @Test
    public void testReportConnectPrivateIpLiteralWithNoPendingPortNotRecorded() {
        // Same private-IP-literal infrastructure-noise filtering as report(), but for the
        // reportConnect() path: a literal IP with no pending port (e.g. a raw socket connect
        // Reactor Netty makes that we never asked for) must not be recorded.
        Context.set(null);
        DNSRecordCollector.reportConnect("10.20.11.143", inetAddress2);
        assertEquals(0, HostnamesStore.getHostnamesAsList().length);
    }

    @Test
    public void testReportConnectStoredSsrfStillRunsUnconditionally() {
        ServiceConfigStore.updateBlocking(true);
        Context.set(null);

        assertThrows(StoredSSRFException.class, () ->
            DNSRecordCollector.reportConnect("dev.aikido", imdsAddress1)
        );
    }

    @Test
    public void testSsrfDetectedForRedirectToPrivateIp() throws Exception {
        // Regression test: a WebClient call to a user-supplied, safe-looking URL that redirects
        // to a private IP must still be caught, even though the redirect target itself never
        // has a pending port (SpringWebClientWrapper only sees the original request).
        // RedirectCollector.report() (called by SpringWebClientRedirectWrapper for each redirect
        // hop) records the chain so SSRFDetector's PrivateIPRedirectFinder fallback can trace the
        // private-IP target back to the tainted origin.
        // Uses attacker-supplied.test rather than example.com since EmptySampleContextObject's
        // own server URL defaults to example.com, which would collide with the origin hostname.
        ServiceConfigStore.updateBlocking(true);
        PendingHostnamesStore.add("attacker-supplied.test", 80);
        Context.set(new EmptySampleContextObject("http://attacker-supplied.test/redirect-me"));

        RedirectCollector.report(
            new URL("http://attacker-supplied.test/redirect-me"),
            new URL("http://169.254.169.254/latest/meta-data/")
        );

        InetAddress imdsResolved = InetAddress.getByAddress(
            "169.254.169.254", new byte[]{(byte) 169, (byte) 254, (byte) 169, (byte) 254});

        Exception exception = assertThrows(SSRFException.class, () ->
            DNSRecordCollector.reportConnect("169.254.169.254", imdsResolved)
        );
        assertEquals("Aikido Zen has blocked a server-side request forgery", exception.getMessage());
    }
}
