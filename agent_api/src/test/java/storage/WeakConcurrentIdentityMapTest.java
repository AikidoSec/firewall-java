package storage;

import dev.aikido.agent_api.storage.WeakConcurrentIdentityMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WeakConcurrentIdentityMapTest {

    @Test
    public void storesAndRetrievesByIdentity() {
        WeakConcurrentIdentityMap<String> map = new WeakConcurrentIdentityMap<>();
        Object key = new Object();
        map.put(key, "value");
        assertEquals("value", map.get(key));
    }

    @Test
    public void distinguishesEqualButNotIdenticalKeys() {
        WeakConcurrentIdentityMap<String> map = new WeakConcurrentIdentityMap<>();
        String a = new String("k");
        String b = new String("k");
        map.put(a, "for-a");
        assertEquals("for-a", map.get(a));
        assertNull(map.get(b));
    }

    @Test
    public void returnsNullForMissingKey() {
        WeakConcurrentIdentityMap<String> map = new WeakConcurrentIdentityMap<>();
        assertNull(map.get(new Object()));
    }

    @Test
    public void overwritesExistingValue() {
        WeakConcurrentIdentityMap<String> map = new WeakConcurrentIdentityMap<>();
        Object key = new Object();
        map.put(key, "first");
        map.put(key, "second");
        assertEquals("second", map.get(key));
    }

    @Test
    public void removesEntry() {
        WeakConcurrentIdentityMap<String> map = new WeakConcurrentIdentityMap<>();
        Object key = new Object();
        map.put(key, "value");
        map.remove(key);
        assertNull(map.get(key));
    }

    @Test
    public void reclaimsEntriesAfterKeyIsCollected() {
        WeakConcurrentIdentityMap<String> map = new WeakConcurrentIdentityMap<>();
        for (int i = 0; i < 1000; i++) {
            map.put(new Object(), "value");
        }
        Object retained = new Object();
        map.put(retained, "retained");
        for (int i = 0; i < 20 && map.size() > 1; i++) {
            System.gc();
            map.size();
        }
        assertEquals("retained", map.get(retained));
        assertTrue(map.size() < 1001, "collected keys should be expunged, size=" + map.size());
    }
}
