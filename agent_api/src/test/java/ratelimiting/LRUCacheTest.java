package ratelimiting;

import static org.junit.jupiter.api.Assertions.*;

import dev.aikido.agent_api.ratelimiting.LRUCache;
import java.util.List;
import org.junit.jupiter.api.Test;

class LRUCacheTest {

    @Test
    void testLRUCacheCreation() {
        LRUCache<String, String> cache = new LRUCache<>(5, 1000);
        assertEquals(0, cache.size(), "Size should be 0 initially");
    }

    @Test
    void testLRUCacheSetAndGetMethods() {
        LRUCache<String, String> cache = new LRUCache<>(5, 1000);

        cache.set("key1", "value1");
        assertEquals("value1", cache.get("key1"), "Value should be retrieved correctly");

        cache.set("key2", "value2");
        assertEquals("value2", cache.get("key2"), "Value should be retrieved correctly");

        assertEquals(2, cache.size(), "Size should be 2 after adding two items");
    }

    @Test
    void testLRUCacheEvictionPolicy() {
        LRUCache<Integer, String> cache = new LRUCache<>(2, 1000);

        cache.set(1, "value1");
        cache.set(2, "value2");
        assertEquals(2, cache.size(), "Size should be 2 after adding two items");

        cache.set(3, "value3");
        assertEquals(2, cache.size(), "Size should be 2 after adding third item");
        assertNull(cache.get(1), "First item should be evicted");
        assertEquals("value2", cache.get(2), "Second item should still be present");
        assertEquals("value3", cache.get(3), "Third item should be present");
    }

    @Test
    void testLRUCacheTTLExpiration() throws InterruptedException {
        LRUCache<String, String> cache = new LRUCache<>(5, 100);

        cache.set("key1", "value1");
        assertEquals("value1", cache.get("key1"), "Value should be retrieved correctly");

        // Wait for TTL to expire
        Thread.sleep(150);

        assertNull(cache.get("key1"), "Value should be None after TTL expiration");
        assertEquals(0, cache.size(), "Size should be 0 after TTL expiration");
    }

    @Test
    void testLRUCacheClearMethod() {
        LRUCache<String, String> cache = new LRUCache<>(5, 1000);

        cache.set("key1", "value1");
        cache.set("key2", "value2");
        assertEquals(2, cache.size(), "Size should be 2 after adding two items");

        cache.clear();
        assertEquals(0, cache.size(), "Size should be 0 after clearing");
        assertNull(cache.get("key1"), "Value should be None after clearing");
        assertNull(cache.get("key2"), "Value should be None after clearing");
    }

    @Test
    void testLRUCacheDeleteMethod() {
        LRUCache<String, String> cache = new LRUCache<>(5, 1000);

        cache.set("key1", "value1");
        cache.set("key2", "value2");
        assertEquals(2, cache.size(), "Size should be 2 after adding two items");

        cache.delete("key1");
        assertEquals(1, cache.size(), "Size should be 1 after deleting one item");
        assertNull(cache.get("key1"), "Value should be None after deletion");
        assertEquals("value2", cache.get("key2"), "Value should be retrieved correctly for remaining item");
    }

    @Test
    void testLRUCacheKeysMethod() {
        LRUCache<String, String> cache = new LRUCache<String, String>(5, 1000);

        cache.set("key1", "value1");
        cache.set("key2", "value2");
        cache.set("key3", "value3");

        List<String> keys = cache.keys();
        assertEquals(3, keys.size());
        assertArrayEquals(new String[] {"key1", "key2", "key3"}, keys.toArray(), "Keys should be retrieved correctly");
    }
}
