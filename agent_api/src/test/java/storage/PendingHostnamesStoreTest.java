package storage;

import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.storage.PendingHostnamesStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import utils.EmptySampleContextObject;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class PendingHostnamesStoreTest {
    @AfterEach
    public void cleanup() {
        PendingHostnamesStore.clear();
    }

    @Test
    public void testGetPortsDoesNotRemoveEntry() {
        PendingHostnamesStore.add("dev.aikido", 443);

        assertEquals(Set.of(443), PendingHostnamesStore.getPorts("dev.aikido"));
        // Reading again still sees it: getPorts() peeks, it doesn't consume.
        assertEquals(Set.of(443), PendingHostnamesStore.getPorts("dev.aikido"));
    }

    @Test
    public void testGetAndRemoveConsumesEntry() {
        PendingHostnamesStore.add("dev.aikido", 443);

        assertEquals(Set.of(443), PendingHostnamesStore.getAndRemove("dev.aikido"));
        assertTrue(PendingHostnamesStore.getAndRemove("dev.aikido").isEmpty());
    }

    @Test
    public void testUnboundedHostnamesDoNotGrowMapForever() {
        // Regression test: entries added outside any incoming-request context (e.g. a
        // WebClient call from a @Scheduled task) never get cleared by WebRequestCollector's
        // per-request clear(). Adding well over the internal cap of distinct hostnames must
        // not let the store grow unboundedly - the oldest, untouched entries get evicted.
        for (int i = 0; i < 2000; i++) {
            PendingHostnamesStore.add("host-" + i + ".example.com", 443);
        }

        // The very first hostnames added, never read again, must have been evicted.
        assertTrue(PendingHostnamesStore.getPorts("host-0.example.com").isEmpty());
        assertTrue(PendingHostnamesStore.getPorts("host-1.example.com").isEmpty());

        // The most recently added hostnames must still be present.
        assertEquals(Set.of(443), PendingHostnamesStore.getPorts("host-1999.example.com"));
    }

    @Test
    public void testReadingAnEntryProtectsItFromEvictionWhileStillInUse() {
        // A dual-stack connect sequence peeks the same hostname's entry more than once (e.g.
        // IPv4 then IPv6 attempt), realistically with only a handful of unrelated hostnames
        // registered on the same thread in between (well under the eviction cap) - not
        // thousands. Each read counts as "recently used", so the entry survives that window.
        PendingHostnamesStore.add("dual-stack.example.com", 443);

        for (int i = 0; i < 10; i++) {
            PendingHostnamesStore.add("host-" + i + ".example.com", 443);
        }
        assertEquals(Set.of(443), PendingHostnamesStore.getPorts("dual-stack.example.com"));

        for (int i = 10; i < 20; i++) {
            PendingHostnamesStore.add("host-" + i + ".example.com", 443);
        }
        assertEquals(Set.of(443), PendingHostnamesStore.getPorts("dual-stack.example.com"));
    }

    @Test
    public void testEntryIsVisibleFromADifferentThread() throws InterruptedException {
        // The whole point of this store being global instead of thread-local: WebClient's
        // "register intent" and "actual connect" steps can run on different OS threads
        // (Reactor Netty's own event-loop dispatch, or an app's own .publishOn()).
        Thread writer = new Thread(() -> PendingHostnamesStore.add("cross-thread.example.com", 443));
        writer.start();
        writer.join();

        Set<Integer> portsSeenFromThisThread = PendingHostnamesStore.getPorts("cross-thread.example.com");
        assertEquals(Set.of(443), portsSeenFromThisThread);
    }

    @Test
    public void testGetContextReturnsWhatWasCapturedAtAddTime() {
        ContextObject context = new EmptySampleContextObject();
        PendingHostnamesStore.add("dev.aikido", 443, context);

        assertSame(context, PendingHostnamesStore.getContext("dev.aikido"));
    }

    @Test
    public void testGetContextIsNullWhenNoneWasCaptured() {
        PendingHostnamesStore.add("dev.aikido", 443, null);

        assertNull(PendingHostnamesStore.getContext("dev.aikido"));
    }

    @Test
    public void testClearRemovesEverything() {
        PendingHostnamesStore.add("dev.aikido", 443);
        PendingHostnamesStore.add("dev.aikido.not", 80);

        PendingHostnamesStore.clear();

        assertTrue(PendingHostnamesStore.getPorts("dev.aikido").isEmpty());
        assertTrue(PendingHostnamesStore.getPorts("dev.aikido.not").isEmpty());
    }
}
