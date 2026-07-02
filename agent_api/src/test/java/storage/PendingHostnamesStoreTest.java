package storage;

import dev.aikido.agent_api.storage.PendingHostnamesStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

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
    public void testUnboundedHostnamesDoNotGrowThreadLocalMapForever() {
        // Entries outside an incoming-request context (e.g. a @Scheduled task) never get
        // cleared otherwise - the oldest, untouched ones must get evicted instead.
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
        // A dual-stack connect sequence peeks the same entry twice; each read must count as
        // "recently used" so it survives unrelated hostnames being added in between.
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
    public void testClearRemovesEverything() {
        PendingHostnamesStore.add("dev.aikido", 443);
        PendingHostnamesStore.add("dev.aikido.not", 80);

        PendingHostnamesStore.clear();

        assertTrue(PendingHostnamesStore.getPorts("dev.aikido").isEmpty());
        assertTrue(PendingHostnamesStore.getPorts("dev.aikido.not").isEmpty());
    }
}
